package de.infonline.lib.iomb.plugins

import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.events.internal.IOLInternetConnectionEventPrivate
import de.infonline.lib.iomb.events.internal.IOLInternetConnectionEventPrivate.IOLInternetConnectionEventPrivateType
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin.Event
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.PerMeasurement
import de.infonline.lib.iomb.util.rx.withPrevious
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Inject

@PerMeasurement
internal class AutoNetworkTracker @Inject constructor(
        scheduler: Scheduler,
        networkMonitor: NetworkMonitor
) : MeasurementPlugin {

    override val events: Observable<Event> = networkMonitor.networkState
            .subscribeOn(scheduler)
            .withPrevious()
            .map { (old, new) ->
                val events = mutableListOf<IOLBaseEvent>()

                when {
                    old == null -> null
                    old.isOnline == new.isOnline -> null
                    new.isOnline -> IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivateType.Established)
                    !new.isOnline -> IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivateType.Lost)
                    else -> null
                }?.let { events.add(it) }

                if (old != null && old.networkType != new.networkType && new.networkType != NetworkMonitor.NetworkType.NO_NETWORK) {
                    events.add(IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivateType.SwitchedInterface))
                }

                events.map { Event.Tracking(iolEvent = it) }.toList<Event>()
            }
            .filter { it.isNotEmpty() }
            .flatMapIterable { it }
            .doOnSubscribe { IOLLog.tag(TAG, public = true).d("Tracking network events!") }
            .doOnNext { IOLLog.tag(TAG).v("Emitting event: %s", it) }
            .doFinally { IOLLog.tag(TAG, public = true).d("No longer tracking network events.") }
            .doOnError { IOLLog.tag(TAG, public = true).e(it, "Error while tracking network events.") }
            .share()


    companion object {
        const val TAG = "AutoNetworkTracker"
    }

}