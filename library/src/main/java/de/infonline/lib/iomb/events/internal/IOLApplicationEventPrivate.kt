package de.infonline.lib.iomb.events.internal

import de.infonline.lib.iomb.IOLEvent
import de.infonline.lib.iomb.events.IOLBaseEvent


internal class IOLApplicationEventPrivate @JvmOverloads constructor(
    type: IOLApplicationEventPrivateType,
    category: String? = null,
    comment: String? = null,
    customParams: Map<String, Any>? = null
) : IOLEvent(
        identifier = ID,
        state = type.state,
        category = category,
        comment = comment,
        customParams = customParams
), IOLLifeCycleEvent {
    enum class IOLApplicationEventPrivateType(override val state: String) : IOLBaseEvent.State {
        Start("start"),
        EnterBackground("enterBackground"),
        EnterForeground("enterForeground"),
        Crashed("crashed");
    }

    companion object {
        internal const val ID = "application"
    }
}