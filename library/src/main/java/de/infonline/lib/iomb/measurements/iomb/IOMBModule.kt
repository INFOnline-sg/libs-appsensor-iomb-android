package de.infonline.lib.iomb.measurements.iomb

import dagger.Module
import dagger.Provides
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin
import de.infonline.lib.iomb.plugins.AppCloseTrigger
import de.infonline.lib.iomb.plugins.AutoAppLifecycleTracker
import de.infonline.lib.iomb.plugins.AutoCrashTracker
import de.infonline.lib.iomb.plugins.AutoNetworkTracker
import de.infonline.lib.iomb.plugins.ClearProofToken
import de.infonline.lib.iomb.util.PerMeasurement

@Module
class IOMBModule {

    @PerMeasurement
    @Provides
    internal fun plugins(
        autoAppLifecycleTracker: AutoAppLifecycleTracker,
        autoNetworkTracker: AutoNetworkTracker,
        clearProofToken: ClearProofToken
    ): Set<@JvmWildcard MeasurementPlugin> = setOf(
        autoAppLifecycleTracker,
        autoNetworkTracker,
        clearProofToken
    )

    @PerMeasurement
    @Provides
    fun setup(setup: IOMBSetup): Measurement.Setup = setup

}