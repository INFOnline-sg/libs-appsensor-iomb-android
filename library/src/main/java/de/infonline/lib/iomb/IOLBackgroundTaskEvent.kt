package de.infonline.lib.iomb


class IOLBackgroundTaskEvent @JvmOverloads constructor(
    type: IOLBackgroundTaskEventType,
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
    enum class IOLBackgroundTaskEventType(val state: String) {
        Start("start"),
        End("end");
    }

    companion object {
        const val ID = "backgroundTask"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLBackgroundTaskEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}