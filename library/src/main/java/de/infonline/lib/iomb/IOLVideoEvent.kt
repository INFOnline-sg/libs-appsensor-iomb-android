package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent

class IOLVideoEvent @JvmOverloads constructor(
    type: IOLVideoEventType,
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
    enum class IOLVideoEventType(override val state: String) : IOLBaseEvent.State {
        Play("play"),
        Pause("pause"),
        Stop("stop"),
        Next("next"),
        Previous("previous"),
        Replay("replay"),
        SeekBack("seekBack"),
        SeekForward("seekForward");
    }

    companion object {
        const val ID = "video"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLVideoEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }

}