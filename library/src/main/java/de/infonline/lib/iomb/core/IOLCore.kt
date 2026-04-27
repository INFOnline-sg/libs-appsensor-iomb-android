package de.infonline.lib.iomb.core

import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.MeasurementInternal
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.rx.latest
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class IOLCore @Inject constructor(
    private val measurementManager: MeasurementManager,
    @IOLibCoreScheduler private val scheduler: Scheduler
) {

    init {
        IOLLog.tag(TAG, public = true).i("Initialized.")
    }

    internal val allMeasurements: Observable<Map<Measurement.Setup, MeasurementInternal?>>
        get() = measurementManager.managedSetups.map { managedSetups ->
            managedSetups
                    .filter { it.measurement != null }
                    .map { it.setup to it.measurement }
                    .toMap()
        }.observeOn(scheduler)

    internal fun createMeasurement(setup: Measurement.Setup, config: LocalConfiguration): Single<MeasurementInternal> {
        IOLLog.tag(TAG, public = true).i("createMeasurement(setup=%s, config=%s)", setup, config)
        require(setup.type == config.type) { "Setup (${setup.type}) and config (${config.type}) don't match!" }
        return measurementManager.createMeasurement(setup, config).observeOn(scheduler)
    }

    internal fun getMeasurement(key: String): Maybe<MeasurementInternal> {
        IOLLog.tag(TAG, public = true).v("getMeasurement(key=%s)", key)
        return measurementManager.managedSetups
                .latest()
                .flatMapMaybe { managedSetups ->
                    val measurement = managedSetups.singleOrNull { it.setup.measurementKey == key }?.measurement
                    if (measurement != null && !measurement.isReleased) Maybe.just(measurement) else Maybe.empty()
                }
                .observeOn(scheduler)
    }

    internal fun deleteMeasurement(key: String): Single<Boolean> {
        IOLLog.tag(TAG, public = true).i("deleteMeasurement(key=%s)", key)
        return measurementManager.deleteMeasurement(key).observeOn(scheduler)
    }

    companion object {
        private const val TAG = "IOLCore"
    }
}