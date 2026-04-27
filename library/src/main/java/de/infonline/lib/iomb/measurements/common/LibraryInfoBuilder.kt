package de.infonline.lib.iomb.measurements.common

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.util.BuildConfigWrap
import de.infonline.lib.iomb.util.PerMeasurement
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@PerMeasurement
internal class LibraryInfoBuilder @Inject constructor(
    val setup: Measurement.Setup
) {

    fun build(configData: ConfigData<*, *>): Single<Info> = Single.fromCallable {
        val debugMode = if (IOLDebug.debugMode) true else null
        Info(
            libVersion = BuildConfigWrap.libraryVersionName,
            configVersion = configData.remoteConfig.getConfigVersion(),
            offerIdentifier = setup.offerIdentifier,
            hybridIdentifier = setup.hybridIdentifier,
            customerData = setup.customerData,
            debug = debugMode
        )
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class Info(
        val libVersion: String,
        val configVersion: String,
        val offerIdentifier: String,
        val hybridIdentifier: String? = null,
        val customerData: String? = null,
        val debug: Boolean? = null
    )
}