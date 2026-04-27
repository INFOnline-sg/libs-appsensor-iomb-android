package de.infonline.lib.iomb

import android.content.Context
import androidx.annotation.VisibleForTesting
import de.infonline.lib.iomb.core.DaggerIOLCoreComponent
import de.infonline.lib.iomb.core.IOLCore
import de.infonline.lib.iomb.core.IOLCoreComponent
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.measurements.iomb.IOMBConfig
import de.infonline.lib.iomb.measurements.iomb.IOMBSetup
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.rx.latest
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

object IOMB {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal lateinit var objGraph: IOLCoreComponent

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal lateinit var iolCore: IOLCore

    internal fun init(context: Context) {
        init { de.infonline.lib.iomb.core.DaggerIOLCoreComponent.factory().create(context.applicationContext) }
    }

    internal fun init(objGrahProvider: () -> IOLCoreComponent) {
        if (this::objGraph.isInitialized) {
            IOLLog.tag(TAG).e("init(context=%s) has already been called.", objGrahProvider)
            return
        }
        val startTime = System.currentTimeMillis()
        synchronized(this) {
            objGraph = objGrahProvider()
            iolCore = objGraph.iolCore
        }
        val stopTime = System.currentTimeMillis()
        IOLLog.tag(TAG).v("IOLCore init took %dms", stopTime - startTime)
    }

    /**
     * A blocking variant of **[create]**.
     * Measurement creation is lightweight, any heavy lifting happens async.
     */
    @JvmStatic
    fun createBlocking(setup: Measurement.Setup): Measurement = create(setup).blockingGet()

    /**
     * Creates a new <code>Measurement</code> instance.
     * Calling this method with the same parameters always returns the same instance.
     *
     * Usually you call this in **[android.app.Application.onCreate]**.
     *
     * Once this has returned a **[Measurement]**, **[get]** and **[getBlocking]** will no longer return null.
     **/
    @JvmStatic
    fun create(setup: Measurement.Setup): Single<Measurement> = iolCore.createMeasurement(setup, IOMBConfig()).map { it as Measurement }

    /**
     * A blocking variant of **[getAll]**
     * Calling this on the main thread is fine as data is readily available.
     * This would only block, if you are simultaneously using **[createBlocking]**.
     */
    @JvmStatic
    fun getAllBlocking(): Map<Measurement.Setup, Measurement?> = getAll().latest().blockingGet()

    /**
     * All known setups, including their instances if running.
     */
    @JvmStatic
    fun getAll(): Observable<Map<Measurement.Setup, Measurement?>> = iolCore.allMeasurements.map { it as Map<Measurement.Setup, Measurement?> }

    /**
     * A blocking variant of **[get]**.
     * Calling this on the main thread is fine, the retrieval is quick.
     */
    @JvmStatic
    fun getBlocking(type: Measurement.Type): Measurement? = get(type).blockingGet()

    /**
     * A specific instance, or null if there is none active for this type.
     * This will start returning null again if you call **[Measurement.release]**
     */
    @JvmStatic
    fun get(type: Measurement.Type): Maybe<Measurement> = get(type.defaultKey)

    internal fun get(key: String): Maybe<Measurement> = iolCore.getMeasurement(key).map { it as Measurement }

    /**
     * This is a blocking variant of  **[delete]**
     * Normally, you don't delete measurements manually.
     *
     * Avoid calling delete on the main thread.
     * Deleting cached data from storage is IO, and doing IO on the main thread is bad ;).
     */
    @JvmStatic
    fun deleteBlocking(type: Measurement.Type): Boolean = delete(type).blockingGet()

    /**
     * Calls **[Measurement.release]** and then deletes all stored data.
     * If you just want to stop a measurement, not loose any cached (and unsent events), use **[Measurement.release]**
     */
    @JvmStatic
    fun delete(type: Measurement.Type): Single<Boolean> = delete(type.defaultKey)

    @JvmStatic
    internal fun delete(key: String): Single<Boolean> = iolCore.deleteMeasurement(key)

    private const val TAG = "IOL"
}