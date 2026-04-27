package de.infonline.lib.iomb.core

import android.content.Context
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.MeasurementInternal
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.util.HotData
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.extensions.deleteAll
import de.infonline.lib.iomb.util.extensions.mutate
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject


internal class MeasurementManager @Inject constructor(
    private val context: Context,
    private val factory: MeasurementFactory,
    @IOLibCoreScheduler private val scheduler: Scheduler
) {

/*    private val initializer: Single<Map<String, ManagedSetup>> = setupRepo.loadSetupData()
            .doOnSubscribe { IOLLog.tag(TAG, public = true).d("Loading configured measurements.") }
            .map { setups ->
                if(setups.isNotEmpty()) IOLLog.tag(TAG, public = true).d("Loading setups: %s", setups)

                setups.mapNotNull { setup ->
                    var faultySetup = false
                    val measurement = if (setup.autoStart) {
                        try {
                            factory.create(setup, null)
                        } catch (e: Exception) {
                            IOLLog.tag(TAG).e(e, "Failed to restore measurement: %s", setup)
                            faultySetup = true
                            null
                        }
                    } else null

                    try {
                        // Confirm that it has a valid config
                        measurement?.configData?.blockingFirst()
                    } catch (e: Exception) {
                        IOLLog.tag(TAG).e(e, "Can't init %s from %s", measurement, setup)
                        try {
                            measurement?.releaseAndClearData(context)
                        } catch (e: Exception) {
                            IOLLog.tag(TAG).e(e, "Failed to release invalid measurement!")
                        }
                        faultySetup = true
                    }

                    return@mapNotNull when {
                        faultySetup -> null
                        else -> setup.measurementKey to ManagedSetup(setup, measurement)
                    }
                }.toMap()

            }
            .doOnSuccess { if(it.isNotEmpty()) IOLLog.tag(TAG).i("%d setups restored: %s", it.size, it) }*/

    private val state: HotData<Map<String, ManagedSetup>> = HotData(Single.just(emptyMap()), scheduler)

    val managedSetups: Observable<List<ManagedSetup>> = state.data.map { it.values.toList() }

/*    init {
        state.data
                .skip(1) // Initial load
                .map { measurements ->
                    measurements.values.map { it.setup }.toSet()
                }
                .flatMapSingle { setupRepo.updateSetupData(it).toSingleDefault(it) }
                .subscribe({ configs ->
                    IOLLog.tag(TAG).d("Stored %d setups: %s", configs.size, configs)
                }, {
                    IOLLog.tag(TAG).e(it, "Failed to store setups.")
                })
    }*/

    fun createMeasurement(
        setup: Measurement.Setup,
        config: LocalConfiguration
    ): Single<MeasurementInternal> = state
            .updateRx { managedSetupMap ->
                val existing = managedSetupMap[setup.measurementKey]?.measurement
                val oldSetup = existing?.setup

                if (isValidSetupUpdate(oldSetup, setup) && existing != null && isValidConfigUpdate(existing, config)) {
                    IOLLog.tag(TAG).i("Updating existing measurement with new config.")
                    existing.updateConfig { config }
                    return@updateRx null
                } else {
                    // TODO test recreation?
                    existing?.let {
                        IOLLog.tag(TAG).i("Releasing existing measurement as it's being replaced.")
                        it.releaseAndClearData(context)
                    }

                    IOLLog.tag(TAG).i("Creating new measurement.")
                    val newMeasurement = factory.create(setup, config)
                    managedSetupMap.mutate {
                        this[setup.measurementKey] = ManagedSetup(setup, newMeasurement)
                    }
                }
            }
            .map { it.newValue.getValue(setup.measurementKey).measurement!! }
            .doOnSubscribe { IOLLog.tag(TAG).v("createMeasurement(setup=%s, config=%s) doOnSubscribe.", setup, config) }
            .doOnSuccess { IOLLog.tag(TAG).d("createMeasurement(setup=%s, config=%s) doOnSuccess.", setup, config) }
            .doOnError { IOLLog.tag(TAG).e(it, "createMeasurement(setup=%s, config=%s) failed.", setup, config) }

    fun deleteMeasurement(key: String): Single<Boolean> = state
            .updateRx {
                it.mutate {
                    val existing = remove(key)?.measurement
                    existing?.releaseAndClearData(context)
                }
            }
            .map { it.oldValue.containsKey(key) != it.newValue.containsKey(key) }
            .doOnSubscribe { IOLLog.tag(TAG).v("deleteMeasurement(key=%s)", key) }
            .doOnSuccess { IOLLog.tag(TAG).d("deleteMeasurement(key=%s) successful=%b", key, it) }
            .doOnError { IOLLog.tag(TAG).e(it, "deleteMeasurement(key=%s) failed.", key) }

    private fun Measurement.releaseAndClearData(context: Context) {
        de.infonline.lib.iomb.util.IOLLog.tag(de.infonline.lib.iomb.core.MeasurementManager.TAG).i("Releasing measurement (setup=%s).", setup)
        release().blockingAwait()

        if(this.setup.type != Measurement.Type.IOMB) {
            val dataDir = setup.getDataDir(context)
            de.infonline.lib.iomb.util.IOLLog.tag(de.infonline.lib.iomb.core.MeasurementManager.TAG).i("Clearing measurement data (path=%s).", dataDir)
            require(dataDir.path.contains(Measurement.Setup.BASE_LIB_DIR)) {
                "Whoa hold your horses! Trying to delete unexpected path!"
            }
            dataDir.deleteAll()
        }
    }

    data class ManagedSetup(
        val setup: Measurement.Setup,
        val measurement: MeasurementInternal?
    )

    private fun isValidSetupUpdate(existing: Measurement.Setup?, newSetup: Measurement.Setup): Boolean {
        if (existing == null) return false
        if (existing::class != newSetup::class) return false

        return existing == newSetup
    }

    private fun isValidConfigUpdate(existing: MeasurementInternal, newConfig: LocalConfiguration): Boolean {
        val existingConfig = existing.configData.blockingFirst().localConfig
        return existingConfig::class == newConfig::class
    }

    companion object {
        private const val TAG = "MeasurementManager"
    }
}