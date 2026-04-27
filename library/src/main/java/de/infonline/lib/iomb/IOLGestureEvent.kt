package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent

class IOLGestureEvent @JvmOverloads constructor(
    type: IOLGestureEventType,
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
    enum class IOLGestureEventType(override val state: String) : IOLBaseEvent.State {
        Shake("shake");
    }

    companion object {
        const val ID = "gesture"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLGestureEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}