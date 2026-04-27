package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent

class IOLGameEvent @JvmOverloads constructor(
    type: IOLGameEventType,
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
    enum class IOLGameEventType(override val state: String) : IOLBaseEvent.State {
        Action("action"),
        Started("started"),
        Finished("finished"),
        Won("won"),
        Lost("lost"),
        NewHighscore("highscore"),
        NewAchievement("achievement");
    }

    companion object {
        const val ID = "game"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLGameEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}