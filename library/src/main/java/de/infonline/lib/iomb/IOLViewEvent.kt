package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent

class IOLViewEvent @JvmOverloads constructor(
    type: IOLViewEventType,
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

    enum class IOLViewEventType(override val state: String) : IOLBaseEvent.State {
        Appeared("appeared"),
        Refreshed("refreshed"),
        Disappeared("disappeared");
    }

    companion object {
        const val ID = "view"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLViewEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}