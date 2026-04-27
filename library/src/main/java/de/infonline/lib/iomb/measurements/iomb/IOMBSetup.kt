package de.infonline.lib.iomb.measurements.iomb

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.util.errors.TypeMissMatchException
import java.lang.Error

@Keep
@JsonClass(generateAdapter = true)
data class IOMBSetup(
    val baseUrl: String,
    @Json(name = "offerIdentifier") override val offerIdentifier: String,
    @Json(name = "hybridIdentifier") override val hybridIdentifier: String? = null,
    @Json(name = "customerData") override val customerData: String? = null,
    ) : Measurement.Setup {

    init {
        require(offerIdentifier.length <= LocalConfiguration.MAX_DATA_LENGTH)
        hybridIdentifier?.let { require(it.length <= LocalConfiguration.MAX_DATA_LENGTH) }
        customerData?.let { require(it.length <= LocalConfiguration.MAX_DATA_LENGTH) }
    }

    @Json(name = "eventServerUrl") override val eventServerUrl: String
        get() = "$baseUrl/base.io"

    @Json(name = "configServerUrl") override val configServerUrl: String
        get() = ""

    override var identifier: String
        get() = Measurement.Type.IOMB.defaultIdentifier
        set(value) = TypeMissMatchException.check(value, identifier)

    override var type: Measurement.Type
        get() = Measurement.Type.IOMB
        set(value) = TypeMissMatchException.check(value, type)

}