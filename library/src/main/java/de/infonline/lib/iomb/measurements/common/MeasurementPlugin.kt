package de.infonline.lib.iomb.measurements.common

import de.infonline.lib.iomb.events.IOLBaseEvent
import io.reactivex.rxjava3.core.Observable

internal interface MeasurementPlugin {
    val events: Observable<Event>

    sealed class Event {
        internal data class Tracking(
            val iolEvent: IOLBaseEvent,
            val isAutoEvent: Boolean = false
        ) : Event()

        internal data class Dispatch(
                val forcedDispatch: Boolean
        ) : Event()
    }
}