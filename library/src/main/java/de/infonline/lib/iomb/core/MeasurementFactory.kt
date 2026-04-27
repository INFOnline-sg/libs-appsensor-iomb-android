package de.infonline.lib.iomb.core

import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.MeasurementInternal
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.measurements.iomb.IOMBComponent
import de.infonline.lib.iomb.measurements.iomb.IOMBConfig
import de.infonline.lib.iomb.measurements.iomb.IOMBSetup
import de.infonline.lib.iomb.util.IOLLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MeasurementFactory @Inject constructor(
    private val iombComponentFactory: IOMBComponent.Factory
) {
    fun create(
        setup: Measurement.Setup,
        initialConfig: LocalConfiguration?
    ): MeasurementInternal {
        IOLLog.tag(TAG)
            .d("Creating measurement instance for %s (initialConfig=%s)", setup, initialConfig)
        return when (setup.type) {
            Measurement.Type.IOMB -> {
                val component = iombComponentFactory.create(
                    setup as IOMBSetup,
                    initialConfig as? IOMBConfig
                )
                component.measurement
            }
            else -> throw Error("Measurement Type is not supported.")
        }
    }

    companion object {
        const val TAG = "MeasurementFactory"
    }
}