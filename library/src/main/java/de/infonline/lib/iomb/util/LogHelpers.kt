package de.infonline.lib.iomb.util

import org.json.JSONObject

internal fun <T> T.toDebugPretty(prettier: T.(T) -> String): String = if (BuildConfigWrap.isDebugBuild) prettier(this) else this.toString()

internal fun String.toDebugPrettyJson(): String = toDebugPretty {
    toFormattedJson()
}


private fun String.toFormattedJson() = toJSONObject().toFormattedJson()

private fun JSONObject.toFormattedJson() = toString(4)

private fun String.toJSONObject() = JSONObject(this)