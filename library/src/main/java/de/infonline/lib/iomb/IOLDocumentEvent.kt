package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent


class IOLDocumentEvent @JvmOverloads constructor(
    type: IOLDocumentEventType,
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
    enum class IOLDocumentEventType(override val state: String) : IOLBaseEvent.State {
        Open("open"),
        Edit("edit"),
        Close("close");
    }

    companion object {
        const val ID = "document"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLDocumentEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}