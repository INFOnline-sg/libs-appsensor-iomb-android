package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent


class IOLPushEvent @JvmOverloads constructor(
    type: IOLPushEventType,
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

    enum class IOLPushEventType(override val state: String) : IOLBaseEvent.State {
        Received("received");
    }

    companion object {
        const val ID = "push"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLPushEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}