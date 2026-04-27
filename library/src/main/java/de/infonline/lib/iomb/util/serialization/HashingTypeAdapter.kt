package de.infonline.lib.iomb.util.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import de.infonline.lib.iomb.measurements.common.config.ConfigData

internal object HashingTypeAdapter {
    @ToJson
    fun toJson(type: ConfigData.Remote.Configuration.HashingType): Int = type.typeValue

    @FromJson
    fun fromJson(typeValue: Int): ConfigData.Remote.Configuration.HashingType = ConfigData.Remote.Configuration.HashingType.fromInt(typeValue)
}
