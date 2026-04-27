package de.infonline.lib.iomb.util.serialization

import com.squareup.moshi.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class SkipBadEnumListAdapter(private val elementAdapter: JsonAdapter<Enum<*>>)
    : JsonAdapter<List<Enum<*>>>() {
    object Factory : JsonAdapter.Factory {
        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi)
                : JsonAdapter<*>? {
            return if (annotations.isEmpty()
                    && Types.getRawType(type) == List::class.java
                    && type is ParameterizedType
                    && type.actualTypeArguments.size == 1
                    && Types.getRawType(type.actualTypeArguments[0]).isEnum) {
                val elementType = Types.collectionElementType(type, List::class.java)
                val elementAdapter = moshi.adapter<Enum<*>>(elementType)
                SkipBadEnumListAdapter(elementAdapter).nullSafe()
            } else {
                null
            }
        }
    }

    override fun fromJson(reader: JsonReader): List<Enum<*>>? {
        val result = mutableListOf<Enum<*>>()
        reader.beginArray()
        while (reader.hasNext()) {
            try {
                val peeked = reader.peekJson()
                elementAdapter.fromJson(peeked)?.let { result.add(it) }
            } catch (ignored: JsonDataException) {
                // Skip bad element ;)
            }
            reader.skipValue()
        }
        reader.endArray()
        return result

    }

    override fun toJson(writer: JsonWriter, value: List<Enum<*>>?) {
        if (value == null) {
            throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginArray()
        for (i in value.indices) {
            elementAdapter.toJson(writer, value[i])
        }
        writer.endArray()
    }
}