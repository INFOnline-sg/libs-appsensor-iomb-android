package de.infonline.lib.iomb.util.extensions

import org.json.JSONArray
import org.json.JSONObject


internal fun JSONArray.toJSONObjects(): List<JSONObject> = (0 until length()).map { get(it) as JSONObject }

internal fun JSONObject.entries(): Sequence<Pair<String, Any>> = this.keys().asSequence().map { key ->
    key to this[key]
}