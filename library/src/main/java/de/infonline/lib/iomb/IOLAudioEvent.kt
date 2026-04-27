package de.infonline.lib.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent

class IOLAudioEvent @JvmOverloads constructor(
    type: IOLAudioEventType,
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

    enum class IOLAudioEventType(override val state: String) : IOLBaseEvent.State {
        Play("play"),
        Pause("pause"),
        Stop("stop"), Next("next"),
        Previous("previous"),
        Replay("replay"),
        SeekBack("seekBack"),
        SeekForward("seekForward");
    }

    companion object {
        const val ID = "audio"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLAudioEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}