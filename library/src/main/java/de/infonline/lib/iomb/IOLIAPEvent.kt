package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent


class IOLIAPEvent @JvmOverloads constructor(
    type: IOLIAPEventType,
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
    enum class IOLIAPEventType(override val state: String) : IOLBaseEvent.State {
        Started("started"),
        Finished("finished"),
        Cancelled("cancelled");
    }

    companion object {
        const val ID = "iap"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLIAPEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}