package de.infonline.lib.iomb.events.internal

import de.infonline.lib.iomb.IOLEvent
import de.infonline.lib.iomb.events.IOLBaseEvent

internal data class IOLInternetConnectionEventPrivate @JvmOverloads constructor(
    private val type: IOLInternetConnectionEventPrivateType,
    private val _category: String? = null,
    private val _comment: String? = null,
    private val _customParams: Map<String, Any>? = null
) : IOLEvent(
        identifier = ID,
        state = type.state,
        category = _category,
        comment = _comment,
        customParams = _customParams
), IOLLifeCycleEvent {

    enum class IOLInternetConnectionEventPrivateType(override val state: String) : IOLBaseEvent.State {
        Established("established"),
        Lost("lost"),
        SwitchedInterface("switchedInterface");
    }

    companion object {
        internal const val ID = "internetConnection"
    }
}