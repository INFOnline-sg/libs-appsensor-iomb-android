package de.infonline.lib.iomb.measurements.common

import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import io.reactivex.rxjava3.core.Observable

internal interface MeasurementInternal : Measurement {

    val configData: Observable<out ConfigData<*, *>>

}