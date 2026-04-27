package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent


class IOLDownloadEvent @JvmOverloads constructor(
    type: IOLDownloadEventType,
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
    enum class IOLDownloadEventType(override val state: String) : IOLBaseEvent.State {
        Cancelled("cancelled"),
        Start("start"),
        Succeeded("succeeded"),
        Failed("failed");
    }

    companion object {
        const val ID = "download"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLDownloadEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}