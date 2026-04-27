package de.infonline.lib.iomb.measurements.common

import com.jakewharton.rx3.replayingShare
import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.MultiIdentifier
import de.infonline.lib.iomb.measurements.RemoteConfigurationInfo
import de.infonline.lib.iomb.measurements.common.caching.EventCache
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.config.ConfigManager
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import de.infonline.lib.iomb.measurements.common.processor.EventProcessor
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.rx.flatMapSingleToMaybe
import de.infonline.lib.iomb.util.rx.latest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

internal abstract class StandardMeasurement<
        ConfigDataT : ConfigData<*, *>,
        ProcessedEventT : EventProcessor.ProcessedEvent,
        DispatchRequestT : EventDispatcher.Request,
        DispatchResponseT : EventDispatcher.Response>
constructor(
    override val setup: Measurement.Setup,
    private val scheduler: Scheduler,
    private val configManager: ConfigManager<ConfigDataT, DispatchResponseT>,
    private val eventCache: EventCache<ProcessedEventT>,
    private val dispatcher: EventDispatcher<ConfigDataT, DispatchRequestT, DispatchResponseT>,
    private val eventProcessor: EventProcessor<ProcessedEventT, ConfigDataT, DispatchRequestT>,
    private val networkMonitor: NetworkMonitor,
    private val multiIdentifierBuilder: MultiIdentifierBuilder,
    private val proofToken: ProofToken? = null,
    plugins: Set<MeasurementPlugin>
) : BaseMeasurement(
    tag = setup.logTag("StandardMeasurement")
) {

    private val submissionQueue = PublishSubject.create<IOLBaseEvent>().toSerialized()
    private val dispatchTrigger = BehaviorSubject.create<Boolean>().toSerialized()

    protected val pluginSubs = CompositeDisposable()

    override val isReleased: Boolean
        get() = pluginSubs.isDisposed

    override val configData: Observable<out ConfigDataT>
        get() = configManager.configuration().observeOn(scheduler)

    override val localConfig: Observable<LocalConfiguration>
        get() = configData.map { it.localConfig }

    override val remoteConfigInfo: Observable<RemoteConfigurationInfo>
        get() = configData.map { configData ->
            val configVersion = configData.remoteConfig.getConfigVersion()
            object : RemoteConfigurationInfo {
                override val configVersion: String = configVersion
            }
        }

    final override val multiIdentifier: Observable<MultiIdentifier> = configManager.configuration()
        .observeOn(scheduler)
        .flatMapSingle {
            multiIdentifierBuilder.build(it).observeOn(scheduler)
        }
        .map { it as MultiIdentifier }
        .replayingShare()

    init {
        // Warm up the multi-identifier cache
        multiIdentifier.observeOn(scheduler)
            .take(1)
            .doOnSubscribe { IOLLog.tag(tag).d("MultiIdentifier warmup...") }
            .doOnNext { IOLLog.tag(tag).d("MultiIdentifier warmedup: %s", it) }
            .ignoreElements()
            .onErrorComplete()
            .subscribe()
            .also { pluginSubs.add(it) }

        configManager.tryUpdateRemoteConfig().subscribe({}, {
            IOLLog.tag(tag).e(it, "Config update failed.")
        })

        submissionQueue
            .observeOn(scheduler)
            .serialize()
            .doOnNext { IOLLog.tag(tag).d("Processing submission: %s", it) }
            .concatMapSingle { event -> configManager.configuration().latest().map { it to event } }
/*            .doOnNext {
                if (this is SZMMeasurement && it.second is IOLEvent) {
                    it.first to processEvent(it.second as IOLEvent)
                    IOLLog.tag(tag).d("Processing event for SZM")
                }
            }*/
            .filter {
                if (proofToken != null) {
                    val isAuditMode = (proofToken.lookupToken() != null)
                    if (isAuditMode && it.first.isMeasuredAudit(it.second)) {
                        IOLLog.tag(tag)
                            .d("AuditMode is active and isMeasuredAudit is true for %s", it.second)
                        return@filter true
                    } else if (!isAuditMode && it.first.isMeasuredRegular(it.second)) {
                        IOLLog.tag(tag).d(
                            "AuditMode is disabled and isMeasuredRegular is true for %s",
                            it.second
                        )
                        return@filter true
                    } else {
                        IOLLog.tag(tag).d(
                            "Event is not measured due to configuration (isAuditMode = %s): %s",
                            isAuditMode,
                            it.second
                        )
                        return@filter false
                    }
                }
                true
            }
            .concatMapSingle { (config, rawEvent) ->
                eventProcessor.process(rawEvent, config).onErrorReturnItem(emptyList())
            }
            .filter { it.isNotEmpty() }
            .concatMapSingle { toStore ->
                eventCache.store(toStore).toSingleDefault(toStore).onErrorReturnItem(emptyList())
            }
            .subscribe({}, {
                IOLLog.tag(tag).e(it, "Processing queue failed.")
            })

        eventCache.events()
            .filter { it.isNotEmpty() }
            .throttleLatest(3, TimeUnit.SECONDS, scheduler)
            .subscribe({
                IOLLog.tag(tag).v("Event cache updated, triggering dispatch.")
                dispatch(forced = false)
            }, {
                IOLLog.tag(tag).e(it, "eventCache.events() threw an exception!")
            })

        configManager.configuration()
            .skip(1) // Initial
            .throttleLatest(3, TimeUnit.SECONDS, scheduler)
            .subscribe({
                IOLLog.tag(tag).v("Configuration changed, triggering dispatch.")
                dispatch(forced = false)
            }, {
                IOLLog.tag(tag).e(it, "configRepo.configuration() threw an exception!")
            })

        dispatchTrigger
            .observeOn(scheduler)
            .doOnNext { IOLLog.tag(tag).v("Dispatch triggered (forced=%b).", it) }
            .flatMapSingle { forced ->
                networkMonitor.isOnline.map { forced to it }
            }
            .filter { (_, isOnline) ->
                if (!isOnline) IOLLog.tag(tag).v("Skipping dispatch, we are offline.")
                isOnline
            }
            .concatMapSingle { (forced, _) ->
                configManager.configuration().latest().map { forced to it }
            }
            .concatMapMaybe { (forced, configData) ->
                attemptDispatch(forced, configData)
            }
            .subscribe({
                IOLLog.tag(tag).d("Dispatch triggered successfully.")
            }, {
                IOLLog.tag(tag).e(it, "Error during dispatch trigger!")
            })

        plugins.forEach { plugin ->
            IOLLog.tag(tag).v("Subscribing to plugin: %s", plugin)
            plugin.events
                .doOnSubscribe { IOLLog.tag(tag).d("Listening to plugin %s", plugin) }
                .subscribeOn(scheduler)
                .zipWith(configManager.configuration()) { t1, t2 -> t1 to t2 }
                .subscribe({

                    if(it.first is MeasurementPlugin.Event.Dispatch) {
                        dispatch((it.first as MeasurementPlugin.Event.Dispatch).forcedDispatch)
                    }

                    if (proofToken != null) {
                        val sendAutoEventsRegular =
                            it.second.remoteConfig.sendAutoEvents?.regular ?: false
                        val sendAutoEventsAudit =
                            it.second.remoteConfig.sendAutoEvents?.audit ?: true
                        val isAuditMode = proofToken.lookupToken() != null

                        IOLLog.tag(tag)
                            .d("sendAutoEvents: %s", it.second.remoteConfig.sendAutoEvents)

                        if (!isAuditMode && !sendAutoEventsRegular) {
                            IOLLog.tag(tag).d("Regular AutoEvent not send: %s", it.first)
                            return@subscribe
                        } else if (isAuditMode && !sendAutoEventsAudit) {
                            IOLLog.tag(tag).d("Audit AutoEvent not send: %s", it.first)
                            return@subscribe
                        }
                    }

                    IOLLog.tag(tag, public = true).d("Processing new plugin event: %s", it.first)
                    logEvent((it.first as MeasurementPlugin.Event.Tracking).iolEvent)

                }, {
                    IOLLog.tag(tag, public = true).e(it, "Plugin emitted error.")
                })
                .also { pluginSubs.add(it) }
        }
    }

    private fun attemptDispatch(forced: Boolean, configData: ConfigDataT): Maybe<Int> {
        val minEvents = if (forced) 1
        else {
            if(proofToken != null) {
                val isAuditMode = !proofToken?.lookupToken().isNullOrEmpty()

                if(isAuditMode) {
                    configData.remoteConfig.cache?.maxBulkEventsAuditMode ?: configData.remoteConfig.getBatchSize()
                } else {
                    configData.remoteConfig.getBatchSize()
                }
            } else {
                configData.remoteConfig.configuration?.minBatchSize ?: 50
            }
        }

        return eventCache
            .drain(minEvents = minEvents)
            .doOnSubscribe { IOLLog.tag(tag, public = true).v("Attempting dispatch.") }
            .doOnSuccess { IOLLog.tag(tag).v("Drained %d events for dispatch.", it.size) }
            .doOnError {
                dispatchErrorCount++
                lastDispatchError = it
                IOLLog.tag(tag, public = true)
                    .e(it, "Error while draining events (errorCount=%d).", dispatchErrorCount)
            }
            .filter { drainedEvents ->
                val minBatchSizeReached = drainedEvents.size >= minEvents
                val allowed = (forced || minBatchSizeReached) && drainedEvents.isNotEmpty()
                if (!allowed) {
                    IOLLog.tag(tag, public = true).v(
                        "Skipping dispatch, minimums not reached (want=%d, got=%d).",
                        minEvents,
                        drainedEvents.size
                    )
                }
                allowed
            }
            .flatMapSingleToMaybe { drainedEvents ->
                IOLLog.tag(tag).v("Preparing dispatch, using configuration: %s", configData)
                eventProcessor.createDispatchRequest(drainedEvents, configData)
                    .flatMap { request ->
                        IOLLog.tag(tag).v("Dispatching request: %s", request)
                        dispatcher.dispatch(request, configData).subscribeOn(scheduler)
                    }
                    .flatMap { response ->
                        IOLLog.tag(tag).v("Dispatching done, response: %s", response)
                        // Mark all drained events as send, the processor may have filtered out some
                        eventCache.markAsSend(drainedEvents).map { response }
                    }
                    .flatMap { configManager.checkRemoteConfig(it) }
                    .map { 1 }
                    .doOnError {
                        dispatchErrorCount++
                        lastDispatchError = it
                        IOLLog.tag(tag)
                            .e(it, "Error while dispatching (errorCount=%d).", dispatchErrorCount)
                    }
            }
            .onErrorComplete()
    }

    override fun updateConfig(update: (LocalConfiguration) -> LocalConfiguration) {
        configManager.updateLocalConfig(update)
            .subscribeOn(scheduler)
            .subscribe({
                IOLLog.tag(tag).i("UserConfig updated to: %s", it)
            }, {
                IOLLog.tag(tag).e(it, "Failed to update UserConfig.")
            })
    }

    override fun logEvent(event: IOLBaseEvent) {
        configManager.tryUpdateRemoteConfig().subscribe({ config ->

/*            if (setup.type == Measurement.Type.ACSAM && (config as ACSAMConfigData.Remote).isVoidConfig) {
                IOLLog.tag(tag, public = true)
                    .i("No remote config available. Event was not added to queue.")
                return@subscribe
            }*/

            if (pluginSubs.isDisposed) {
                IOLLog.tag(tag, public = true)
                    .w("Submission to released measurement instance: %s", event)
            } else {
                IOLLog.tag(tag, public = true).i("Adding new event to queue: %s", event)
            }

            submissionQueue.onNext(event)
        }, {
            IOLLog.tag(tag).e(it, "Config update failed.")
        })
    }

    override fun dispatch(forced: Boolean) {
        if (forced) {
            IOLLog.tag(tag, public = true).i("dispatch(forced=%b)", forced)
        } else {
            IOLLog.tag(tag).d("dispatch(forced=%b)", forced)
        }
        dispatchTrigger.onNext(forced)
    }

    override fun release(): Completable = Completable
        .fromCallable {
            synchronized(pluginSubs) {
                if (pluginSubs.isDisposed) throw IllegalStateException("release() was already called.")
                pluginSubs.dispose()
            }
        }
        .subscribeOn(scheduler)
        .doOnSubscribe { IOLLog.tag(tag).i("release()") }
        .doOnComplete {
            submissionQueue.onComplete()
            dispatchTrigger.onComplete()
        }
        .andThen(eventProcessor.release())
        .andThen(dispatcher.release())
        .andThen(eventCache.release())
        .onErrorComplete { it is IllegalStateException }
}