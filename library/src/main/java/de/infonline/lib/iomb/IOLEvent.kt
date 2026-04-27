package de.infonline.lib.iomb

import android.content.Context
import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.util.extensions.sanitize
import org.json.JSONObject

abstract class IOLEvent(
        override val identifier: String,
        category: String?,
        state: String?,
        comment: String?,
        customParams: Map<String, Any>?
) : IOLBaseEvent {

    internal var iabConsent: String? = null

    override val category: String? = category?.sanitize(disallowedChars = "[^\\w,/-]", maxLength = MAX_LENGTH)
    override val state: String? = state?.sanitize(disallowedChars = null, maxLength = MAX_LENGTH)
    override val comment: String? = comment?.sanitize(disallowedChars =  "[^ -~]", maxLength = MAX_LENGTH)
    val customParams: Map<String, Any>? = customParams?.sanitize(maxLength = MAX_LENGTH)

    override fun toString(): String = "Event(identifier=$identifier, state=$state, category=$category, comment=$comment, customParams=$customParams)"

    override fun buildParameters(context: Context): Map<String, Any> {
        // TODO clean up empty keys?
        return customParams?.let {
            mapOf(
                    "customParameter" to JSONObject(customParams).toString()
            )
        } ?: emptyMap()
    }

    companion object {
        const val MAX_LENGTH = 255

        private fun Map<String, Any>.sanitize(maxLength: Int = MAX_LENGTH): Map<String, Any> = this
                .map { (key, value) ->
                    val sanitizedKey = key.sanitize(disallowedChars = null, maxLength = maxLength)
                    val sanitizedValue = value.let {
                        when (it) {
                            is String -> it.sanitize(disallowedChars = null, maxLength = maxLength)
                            else -> it
                        }
                    }
                    sanitizedKey to sanitizedValue
                }
                .toMap()
    }
}