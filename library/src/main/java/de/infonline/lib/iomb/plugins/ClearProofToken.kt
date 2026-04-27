package de.infonline.lib.iomb.plugins

import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin.Event
import de.infonline.lib.iomb.measurements.common.ProofToken
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.LifecycleOwnerForProcess
import de.infonline.lib.iomb.util.PerMeasurement
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.ReplaySubject
import javax.inject.Inject

@PerMeasurement
internal class ClearProofToken @Inject constructor(
    scheduler: Scheduler,
    @LifecycleOwnerForProcess private val lifecycleOwner: LifecycleOwner,
    proofToken: ProofToken
) : MeasurementPlugin {

    private val publisher = ReplaySubject.create<Event>().toSerialized()
    override val events: Observable<Event> = publisher
            .doOnSubscribe {
                AndroidSchedulers.mainThread().scheduleDirect {
                    lifecycleOwner.lifecycle.addObserver(lifecycleMonitor)
                }
            }
            .doFinally {
                AndroidSchedulers.mainThread().scheduleDirect {
                    lifecycleOwner.lifecycle.removeObserver(lifecycleMonitor)
                }
            }
            .observeOn(scheduler)
            .doOnError { IOLLog.tag(TAG).e(it, "Error while tracking lifecycle.") }
            .share()

    private val lifecycleMonitor = object : LifecycleObserver {
        @Keep
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            IOLLog.tag(TAG).v("Clear cached ProofToken.")
            proofToken.clearCachedToken()
        }
    }

    companion object {
        const val TAG = "ClearProofToken"
    }
}