package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent


class IOLLoginEvent @JvmOverloads constructor(
    type: IOLLoginEventType,
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
    enum class IOLLoginEventType(override val state: String) : IOLBaseEvent.State {
        Succeeded("succeeded"),
        Failed("failed"),
        Logout("logout");
    }

    companion object {
        const val ID = "login"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLLoginEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}