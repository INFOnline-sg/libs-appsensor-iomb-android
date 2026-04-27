package de.infonline.lib.iomb.measurements.common

import dagger.Module
import dagger.Provides
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.util.PerMeasurement
import de.infonline.lib.iomb.util.rx.SchedulersCustom
import io.reactivex.rxjava3.core.Scheduler

@Module
internal class CommonModule {

    @PerMeasurement
    @Provides
    fun measurementScheduler(setup: Measurement.Setup): Scheduler {
        return SchedulersCustom.customScheduler(2, setup.measurementKey)
    }

}