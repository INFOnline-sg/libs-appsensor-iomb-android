package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent

class IOLHardwareButtonEvent @JvmOverloads constructor(
    type: IOLHardwareButtonEventType,
    category: String? = null,
    comment: String? = null,
    customParams: Map<String, Any>? = null
) : IOLEvent(
        identifier = ID,
        state = type.state,
        category = category,
        comment = comment,
        customParams = customParams
) {

    enum class IOLHardwareButtonEventType(override val state: String) : IOLBaseEvent.State {
        Pushed("pushed");
    }

    companion object {
        const val ID = "hardwareButton"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLHardwareButtonEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}