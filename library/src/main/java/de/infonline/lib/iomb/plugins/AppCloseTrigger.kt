package de.infonline.lib.iomb.plugins

import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin.Event
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.LifecycleOwnerForProcess
import de.infonline.lib.iomb.util.PerMeasurement
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.ReplaySubject
import javax.inject.Inject

@PerMeasurement
internal class AppCloseTrigger @Inject constructor(
        scheduler: Scheduler,
        @LifecycleOwnerForProcess private val lifecycleOwner: LifecycleOwner
) : MeasurementPlugin {


    private val publisher = ReplaySubject.create<Event>().toSerialized()
    override val events: Observable<Event> = publisher
            .doOnSubscribe {
                AndroidSchedulers.mainThread().scheduleDirect {
                    lifecycleOwner.lifecycle.addObserver(lifecycleMonitor)
                    IOLLog.tag(TAG, public = true).d("Monitoring lifecycle!")
                }
            }
            .doFinally {
                AndroidSchedulers.mainThread().scheduleDirect {
                    lifecycleOwner.lifecycle.removeObserver(lifecycleMonitor)
                    IOLLog.tag(TAG, public = true).d("No longer monitoring lifecycle.")
                }
            }
            .observeOn(scheduler)
            .doOnNext { IOLLog.tag(TAG).d("Emitting event: %s.", it) }
            .doOnError { IOLLog.tag(TAG).e(it, "Error while tracking lifecycle.") }
            .share()

    private val lifecycleMonitor = object : LifecycleObserver {
        @Keep
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onMoveToBackground() {
            IOLLog.tag(TAG).d("Lifecycle event: App EnterBackground.")
            publisher.onNext(Event.Dispatch(forcedDispatch = true))
        }
    }

    companion object {
        const val TAG = "AppCloseTrigger"
    }
}