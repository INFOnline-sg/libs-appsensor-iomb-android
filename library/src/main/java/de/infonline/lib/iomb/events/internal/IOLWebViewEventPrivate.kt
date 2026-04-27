package de.infonline.lib.iomb.events.internal

import de.infonline.lib.iomb.IOLEvent
import de.infonline.lib.iomb.events.IOLBaseEvent


internal class IOLWebViewEventPrivate @JvmOverloads constructor(
    type: IOLWebViewEventPrivateType,
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
    enum class IOLWebViewEventPrivateType(override val state: String) : IOLBaseEvent.State {
        Init("init");
    }

    companion object {
        internal const val ID = "webView"
    }
}