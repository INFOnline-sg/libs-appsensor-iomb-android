package de.infonline.lib.iomb.measurements.common

import androidx.annotation.VisibleForTesting
import de.infonline.lib.iomb.plugins.AutoCrashTracker
import de.infonline.lib.iomb.util.IOLLog
import io.reactivex.rxjava3.plugins.RxJavaPlugins

internal abstract class BaseMeasurement constructor(
    val tag: String
) : MeasurementInternal {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var dispatchErrorCount: Int = 0

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var lastDispatchError: Throwable? = null

    init {
        //  If BaseMeasurement does the plugin work, we need to pass the setup value and ensure that the submission queue has a subscription.

        RxJavaPlugins.setErrorHandler { error ->
            IOLLog.tag(AutoCrashTracker.TAG).i("Tracking uncaught exception: %s", error)
        }
    }

}