package de.infonline.lib.iomb.util.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

object InstantAdapter {
    @ToJson
    fun toJson(instant: Instant): String = instant.toString()

    @FromJson
    fun fromJson(instant: String): Instant = Instant.parse(instant)
}
