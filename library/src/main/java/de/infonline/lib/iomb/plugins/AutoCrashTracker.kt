package de.infonline.lib.iomb.plugins

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import de.infonline.lib.iomb.events.internal.IOLApplicationEventPrivate
import de.infonline.lib.iomb.events.internal.IOLApplicationEventPrivate.*
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin.*
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.PerMeasurement
import de.infonline.lib.iomb.util.serialization.fromFile
import de.infonline.lib.iomb.util.serialization.toFile
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.File
import java.lang.reflect.Type
import java.time.Instant
import javax.inject.Inject

@PerMeasurement
internal class AutoCrashTracker @Inject constructor(
    setup: Measurement.Setup,
    scheduler: Scheduler,
    context: Context,
    private val moshi: Moshi
) : MeasurementPlugin {

    private val adapter by lazy {
        val listMyData: Type = Types.newParameterizedType(List::class.java, CrashEvent::class.java)
        moshi.adapter<List<CrashEvent>>(listMyData)
    }
    private val crashDir = File(setup.getDataDir(context), "crashes")
    private val crashEventFile = File(crashDir, "crashes.json")

    init {
        try {
            crashDir.parentFile!!.mkdirs()
        } catch (e: Exception) {
            IOLLog.tag(TAG).e("Failed to setup temp storage for crash events: %s", crashDir)
        }
    }

    private val publisher = PublishSubject.create<Event>().toSerialized()
    override val events: Observable<Event> = publisher
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .startWith(Observable.create { emitter ->
                val crashEvent = restoreCrash()
                if (crashEvent != null) {
                    emitter.onNext(
                            Event.Tracking(
                                    iolEvent = IOLApplicationEventPrivate(IOLApplicationEventPrivateType.Crashed)
                            )
                    )
                }
                emitter.onComplete()
            })
            .doOnSubscribe {
                IOLLog.tag(TAG).v("Event source has subscriber!")
                setupTracking()
            }
            .doOnNext { IOLLog.tag(TAG, public = true).i("Emitting crash event: %s.", it) }
            .doFinally {
                IOLLog.tag(TAG).v("Event source terminated.")
                teardownTracking()
                publisher.onComplete()
            }
            .doOnError { IOLLog.tag(TAG, public = true).e(it, "Error while tracking crashes.") }
            .share()

    private fun storeCrash(error: Throwable) {
        val crashEvent = CrashEvent(
                errorInfo = error.toString(),
                message = error.message
        )
        synchronized(this@AutoCrashTracker) {
            adapter.toFile(listOf(crashEvent), crashEventFile)
        }
    }

    private fun restoreCrash(): List<CrashEvent>? {
        return synchronized(this@AutoCrashTracker) {
            if (!crashEventFile.exists()) null
            else adapter.fromFile(crashEventFile)
        }
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class CrashEvent(
            @Json(name = "errorInfo") val errorInfo: String,
            @Json(name = "message") val message: String?,
            @Json(name = "createdAt") val createdAt: Instant = Instant.now()
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var ourHandler: OurHandler? = null

    internal class OurHandler constructor(
            internal val originalHandler: Thread.UncaughtExceptionHandler?,
            private val crashCallback: (Throwable) -> Unit
    ) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, error: Throwable) {
            IOLLog.tag(TAG).i("Tracking uncaught exception: %s", error)
            crashCallback(error)
            originalHandler?.uncaughtException(thread, error)
        }
    }

    private fun setupTracking() {
        synchronized(this) {
            if (Thread.getDefaultUncaughtExceptionHandler() == ourHandler && ourHandler != null) {
                IOLLog.tag(TAG).e("Tried to register duplicate exception handler!")
                return
            }

            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            IOLLog.tag(TAG).d("Wrapping default handler: %s", defaultHandler)
            ourHandler = OurHandler(defaultHandler) { storeCrash(it) }
                    .also { Thread.setDefaultUncaughtExceptionHandler(it) }

            IOLLog.tag(TAG).d("Crash tracking is setup!")
        }
    }

    private fun teardownTracking() {
        synchronized(this) {
            Thread.getDefaultUncaughtExceptionHandler()?.let {
                if (it != ourHandler) {
                    IOLLog.tag(TAG).i("Current default exception handler isn't us. Hands off!")
                } else {
                    IOLLog.tag(TAG).i("Removed our exception handler.")
                    Thread.setDefaultUncaughtExceptionHandler(ourHandler?.originalHandler)
                    ourHandler = null
                }
            }
        }
    }

    companion object {
        const val TAG = "AutoCrashTracker"
    }
}