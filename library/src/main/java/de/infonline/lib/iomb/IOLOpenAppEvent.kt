package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent


class IOLOpenAppEvent @JvmOverloads constructor(
    type: IOLOpenAppEventType,
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
    enum class IOLOpenAppEventType(override val state: String) : IOLBaseEvent.State {
        Maps("maps"),
        Other("other");
    }

    companion object {
        const val ID = "openApp"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLOpenAppEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}