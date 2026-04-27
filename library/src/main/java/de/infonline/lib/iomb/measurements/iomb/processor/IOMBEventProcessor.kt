package de.infonline.lib.iomb.measurements.iomb.processor

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.events.internal.IOLLifeCycleEvent
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.ClientInfoBuilder
import de.infonline.lib.iomb.measurements.common.LibraryInfoBuilder
import de.infonline.lib.iomb.measurements.common.ProofToken
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import de.infonline.lib.iomb.measurements.common.processor.EventProcessor
import de.infonline.lib.iomb.measurements.common.processor.StandardProcessedEvent
import de.infonline.lib.iomb.measurements.iomb.config.IOMBConfigData
import de.infonline.lib.iomb.measurements.iomb.dispatch.IOMBEventDispatcher
import de.infonline.lib.iomb.util.HashHelper.toMD5
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.PerMeasurement
import de.infonline.lib.iomb.util.TimeStamper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Singles
import io.reactivex.rxjava3.subjects.ReplaySubject
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

@PerMeasurement
internal class IOMBEventProcessor @Inject constructor(
    setup: Measurement.Setup,
    private val scheduler: Scheduler,
    private val moshi: Moshi,
    private val libraryInfoBuilder: LibraryInfoBuilder,
    private val clientInfoBuilder: ClientInfoBuilder,
    private val timeStamper: TimeStamper,
    private val proofToken: ProofToken? = null,
) : EventProcessor<StandardProcessedEvent, IOMBConfigData, IOMBEventDispatcher.Request> {

    private val tag = setup.logTag("IOMBEventProcessor")

    private val mappingAdapter by lazy {
        moshi
            .newBuilder()
            .add(NetworkMonitor.NetworkTypeAdapter())
            .build()
            .adapter(IOMBSchema::class.java)
    }

    private val processedEventAdapter by lazy {
        moshi
            .newBuilder()
            .build()
            .adapter<List<StandardProcessedEvent>>(
                Types.newParameterizedType(
                    List::class.java,
                    StandardProcessedEvent::class.java
                )
            )

            .indent("    ")
    }

    internal var lastEvent: ReplaySubject<PartialEventData> = ReplaySubject.createWithSize(1)

    internal data class PartialEventData(val category: String?, val comment: String?)

    override fun process(
        event: IOLBaseEvent,
        configData: IOMBConfigData
    ): Single<List<StandardProcessedEvent>> = Single
        .fromCallable { configData.isMeasuredRegular(event) }
        .subscribeOn(scheduler)
        .filter { isEventAllowed ->
            if (!isEventAllowed) IOLLog.tag(tag, public = true)
                .d("Discarding event, not enabled in config: %s", event)
            isEventAllowed
        }
        .flatMapSingle {
            Singles.zip(
                clientInfoBuilder.build(configData),
                libraryInfoBuilder.build(configData)
            )
        }
        .map { (clientInfo, libraryInfo) ->
            val nowUTC = timeStamper.nowUTC

            val eventIdString = if (event.state != null) {
                "${event.identifier}.${event.state}"
            } else {
                event.identifier
            }

            val contentCode = if (event is IOLLifeCycleEvent) {
                null
            } else if (!event.category.isNullOrBlank()) {
                event.category
            } else {
                "Leercode_nichtzuordnungsfaehig"
            }

            val comment = if (configData.remoteConfig.specialParameters?.comment == true) {
                event.comment
            } else {
                null
            }

            lastEvent.onNext(PartialEventData(contentCode, comment))

            val schemaSiteInformation = IOMBSchema.SiteInformation(
                country = clientInfo.locale.country.lowercase(Locale.getDefault()),
                comment = comment,
                contentCode = contentCode,
                event = eventIdString,
                site = libraryInfo.offerIdentifier,
            )

            val schemaDeviceInformation = IOMBSchema.DeviceInformation(
                osVersion = clientInfo.osVersion,
                deviceName = clientInfo.deviceName,
            )

            val schemaTechnicalInformation = IOMBSchema.TechnicalInformation(
                debugModus = libraryInfo.debug ?: false,
                sensorSDKVersion = libraryInfo.libVersion,
            )

            val mapping = IOMBSchema(
                siteInformation = schemaSiteInformation,
                deviceInformation = schemaDeviceInformation,
                technicalInformation = schemaTechnicalInformation,
            )



            mapping.technicalInformation.checksumMD5 = buildChecksum(mapping)

            @Suppress("UNCHECKED_CAST")
            val mappingJson = mappingAdapter.toJsonValue(mapping) as Map<String, Any>

            listOf(
                StandardProcessedEvent(
                    createdAt = nowUTC,
                    persist = configData.remoteConfig.offlineMode,
                    event = mappingJson
                )
            )
        }
        .switchIfEmpty(Single.just(emptyList()))
        .doOnSuccess {
            val isAuditMode = (proofToken?.lookupToken() != null)

            if (isAuditMode) {
                IOLLog.tag(tag, public = true)
                    .i("Processed %s to %s", event, processedEventAdapter.toJson(it))
            } else {
                IOLLog.tag(tag).v("Processed %s", event)
            }
        }
        .doOnError { IOLLog.tag(tag).e(it, "Error while processing event.") }


    override fun createDispatchRequest(
        events: List<EventProcessor.ProcessedEvent>,
        configData: IOMBConfigData
    ): Single<IOMBEventDispatcher.Request> {
        return Single.just(IOMBEventDispatcher.Request(events)).subscribeOn(scheduler)
    }

    override fun release(): Completable {
        return Completable.fromCallable {
            lastEvent.onComplete()
        }
    }

    private fun buildChecksum(mapping: IOMBSchema): String {
        val di = JSONObject()
        di.put("pn", mapping.deviceInformation.osIdentifier)
        di.put("pv", mapping.deviceInformation.osVersion)
        if (mapping.deviceInformation.deviceName != null) di.put(
            "to",
            mapping.deviceInformation.deviceName
        )

        val si = JSONObject()
        si.put("cn", mapping.siteInformation.country)
        if (mapping.siteInformation.comment != null) si.put(
            "co",
            mapping.siteInformation.comment
        )
        if (mapping.siteInformation.contentCode != null) si.put(
            "cp",
            mapping.siteInformation.contentCode
        )
        si.put("dc", mapping.siteInformation.distributionChannel)
        if (mapping.siteInformation.event != null) si.put("ev", mapping.siteInformation.event)
        si.put("pt", mapping.siteInformation.pixelType)
        si.put("st", mapping.siteInformation.site)

        val ti = JSONObject()
        ti.put("dm", mapping.technicalInformation.debugModus)
        ti.put("it", mapping.technicalInformation.integrationType)
        ti.put("vr", mapping.technicalInformation.sensorSDKVersion)

        val checksumInput = JSONObject()
        checksumInput.put("di", di)
        checksumInput.put("si", si)
        checksumInput.put("sv", mapping.schemaVersion)
        checksumInput.put("ti", ti)

        return checksumInput.toString().toMD5()
    }
}