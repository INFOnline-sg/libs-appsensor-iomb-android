package de.infonline.lib.iomb.util.rx

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Executors

internal object SchedulersCustom {

    fun customScheduler(poolSize: Int, poolName: String? = null): Scheduler {
        val executor = if (poolName != null) {
            Executors.newFixedThreadPool(poolSize, NamedThreadFactory(poolName))
        } else {
            Executors.newFixedThreadPool(poolSize)
        }
        return Schedulers.from(executor)
    }

}