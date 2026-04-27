package de.infonline.lib.iomb.measurements.common

import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.util.rx.SchedulersCustom
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Inject

class SchedulerFactory @Inject constructor(

) {

    fun createScheduler(setup: Measurement.Setup): Scheduler {
        return SchedulersCustom.customScheduler(2, setup.identifier)
    }

}