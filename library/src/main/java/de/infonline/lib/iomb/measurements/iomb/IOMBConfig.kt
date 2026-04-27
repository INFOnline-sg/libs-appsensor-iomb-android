package de.infonline.lib.iomb.measurements.iomb

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.util.errors.TypeMissMatchException

@Keep
@JsonClass(generateAdapter = true)
class IOMBConfig : LocalConfiguration {

    override var type: Measurement.Type
        get() = Measurement.Type.IOMB
        set(value) = TypeMissMatchException.check(value, type)
}