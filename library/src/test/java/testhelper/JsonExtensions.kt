package testhelper

import okio.ByteString.Companion.encode
import okio.buffer
import okio.sink
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun String.toFormattedJson() = if (this.trim().startsWith("[")) {
    toJSONArray().toFormattedJson()
} else {
    toJSONObject().toFormattedJson()
}

fun JSONArray.toFormattedJson() = toString(4)

fun JSONObject.toFormattedJson(): String {
    val keyValues = mutableListOf<String>()
    keys().forEach {
        keyValues.add(it as String)
    }

    return keyValues.sorted()
            .map { key: String ->
                val value = this[key]
                key to value
            }
            .toList()
            .let { pairs ->
                val linked = LinkedHashMap<String?, Any?>()
                for (pair in pairs) {
                    linked[pair.first] = pair.second
                }
                JSONObject(linked).toString(4)
            }
}

fun String.toJSONObject() = JSONObject(this)

fun String.toJSONArray() = JSONArray(this)

fun Map<String, Any>.toFormattedJson() = toJSONObject().toFormattedJson()

fun Map<String, Any>.toJSONObject() = JSONObject(this)

fun String.writeToFile(file: File) = encode().let { text ->
    require(!file.exists())
    file.parentFile?.mkdirs()
    file.createNewFile()
    file.sink().buffer().use { it.write(text) }
}

fun JSONObject.putPlaceHolders(
        vararg keys: String,
        placeholderProvider: (String, String) -> String = { key, value -> "placeholder_$key" }
): JSONObject {

    keys.forEach { key ->
        if (has(key)) {
            put(key, placeholderProvider(key, getString(key)))
        }
    }

    return this
}

fun JSONArray.asSequence() = (0 until length()).map { get(it) as JSONObject }.asSequence()