package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent


class IOLUploadEvent @JvmOverloads constructor(
    type: IOLUploadEventType,
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
    enum class IOLUploadEventType(override val state: String) : IOLBaseEvent.State {
        Cancelled("cancelled"),
        Start("start"),
        Succeeded("succeeded"),
        Failed("failed");

    }

    companion object {
        const val ID = "upload"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLUploadEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}