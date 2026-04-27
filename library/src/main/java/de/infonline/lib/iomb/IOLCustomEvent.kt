package de.infonline.lib.iomb


class IOLCustomEvent @JvmOverloads constructor(
        state: String?,
        category: String? = null,
        comment: String? = null,
        customParams: Map<String, Any>? = null
) : IOLEvent(
        identifier = ID,
        state = state,
        category = category,
        comment = comment,
        customParams = customParams
) {

    companion object {
        const val ID = "custom"

        @Deprecated(
                message = "Use ID property",
                replaceWith = ReplaceWith(expression = "IOLCustomEvent.ID"),
                level = DeprecationLevel.WARNING
        )
        const val eventIdentifier = ID
    }
}