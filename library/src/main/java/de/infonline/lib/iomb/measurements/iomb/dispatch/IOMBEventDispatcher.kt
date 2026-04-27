package de.infonline.lib.iomb.measurements.iomb.dispatch

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import de.infonline.lib.iomb.BuildConfig
import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.ConfigManager
import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import de.infonline.lib.iomb.measurements.common.processor.EventProcessor
import de.infonline.lib.iomb.measurements.iomb.config.IOMBConfigData
import de.infonline.lib.iomb.util.BuildConfigWrap
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.PerMeasurement
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.ReplaySubject
import io.reactivex.rxjava3.subjects.Subject
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Type
import javax.inject.Inject


@PerMeasurement
internal class IOMBEventDispatcher @Inject constructor(
    private val setup: Measurement.Setup,
    moshi: Moshi,
) : EventDispatcher<
        IOMBConfigData,
        IOMBEventDispatcher.Request,
        EventDispatcher.Response
        > {

    private val tag = setup.logTag("IOMBEventDispatcher")

    private val adapter by lazy {
        val type: Type =
            Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        moshi.adapter<Map<String, Any>>(type)
    }

    private val api by lazy {
        val client: OkHttpClient = OkHttpClient.Builder().apply {
            if (BuildConfigWrap.isDebugBuild) {
                val logging =
                    HttpLoggingInterceptor { message -> IOLLog.tag(tag).v(message) }.apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                addInterceptor(logging)
            }
        }.build()

        val retrofit = Retrofit.Builder().apply {
            client(client)
            // This is overriden by the @Url parameter
            // Retrofit does not allow omitting this
            // We can't just add the URL here because retrofit demands a trailing slash (.../tx.io works, .../tx.io/ doesn't).
            baseUrl("https://0.0.0.0")
            addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
        }.build()

        retrofit.create(IOMBEventApi::class.java)
    }
    private var dispatchMonitor: Subject<Pair<EventDispatcher.Request, Any>>?

    init {
        if (BuildConfigWrap.isDebugBuild) {
            dispatchMonitor = ReplaySubject.create()
            IOLDebug.dispatchSpy[setup.measurementKey] = dispatchMonitor!!
        } else {
            dispatchMonitor = null
        }
    }

    override fun dispatch(
        request: Request,
        config: IOMBConfigData
    ): Single<out EventDispatcher.Response> = Single
        .fromCallable {
            IOLLog.tag(tag, public = true)
                .d("Dispatching to %d events to %s", request.events.size, setup.eventServerUrl)

            if (BuildConfig.DRY_RUN) {
                IOLLog.tag(tag).w("DRY_RUN enabled, assuming dispatch was successful!")
                return@fromCallable Response(configStatusCode = ConfigManager.Status.OK)
            }

            if (request.events.isEmpty()) {
                IOLLog.tag(tag).w("Skipping dispatch request, because it contained 0 events.")
                return@fromCallable Response(configStatusCode = ConfigManager.Status.OK)
            }

            request.events.forEach { _ ->
                val event: String = adapter.toJson(request.events[0].event)

                IOLLog.tag(tag).i("Posting event: %s", event)

                api.postEvent(
                    url = setup.eventServerUrl,
                    event = event.toRequestBody()
                ).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                        response.code().apply {
                            IOLLog.tag(tag, public = true)
                                .i("Received response (code=%d): %s", this, response)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        IOLLog.tag(tag).e(t.message)
                    }
                })
            }

            Response(configStatusCode = ConfigManager.Status.OK)
        }
        .doOnError {
            IOLLog.tag(tag).e(it, "Dispatch error for %s", request)
            if (BuildConfigWrap.isDebugBuild) dispatchMonitor?.onNext(request to it)
        }
        .doOnSuccess {
            IOLLog.tag(tag).v("Dispatch successful for %s", request)
            if (BuildConfigWrap.isDebugBuild) dispatchMonitor?.onNext(request to it)
        }

    override fun release(): Completable = Completable.fromCallable {
        if (BuildConfigWrap.isDebugBuild) {
            dispatchMonitor?.onComplete()
            IOLDebug.dispatchSpy.remove(setup.measurementKey)
        }
    }

    data class Request(
        override val events: List<EventProcessor.ProcessedEvent>
    ) : EventDispatcher.Request

    data class Response(
        override val configStatusCode: ConfigManager.Status
    ) : EventDispatcher.Response

}