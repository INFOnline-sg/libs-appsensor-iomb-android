package de.infonline.lib.iomb

import androidx.annotation.Keep
import de.infonline.lib.iomb.events.IOLBaseEvent


class IOLAdvertisementEvent @JvmOverloads constructor(
    type: IOLAdvertisementEventType,
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

    @Keep
    enum class IOLAdvertisementEventType(override val state: String) : IOLBaseEvent.State {
        Open("open"),
        Close("close");
    }

    companion object {
        const val ID = "advertisement"

        @Keep
        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLAdvertisementEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}