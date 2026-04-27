package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent


class IOLDataEvent @JvmOverloads constructor(
    type: IOLDataEventType,
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
    enum class IOLDataEventType(override val state: String) : IOLBaseEvent.State {
        Cancelled("cancelled"),
        Refresh("refresh"),
        Succeeded("succeeded"),
        Failed("failed");
    }

    companion object {
        const val ID = "data"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLDataEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}