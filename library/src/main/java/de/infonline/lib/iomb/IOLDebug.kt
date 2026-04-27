package de.infonline.lib.iomb

import android.util.Log
import androidx.annotation.VisibleForTesting
import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import de.infonline.lib.iomb.util.BuildConfigWrap
import de.infonline.lib.iomb.util.HotData
import de.infonline.lib.iomb.util.extensions.toStackTraceString
import io.reactivex.rxjava3.subjects.Subject

object IOLDebug {

    @JvmStatic
    var debugMode: Boolean = BuildConfigWrap.isDebugBuild

    @JvmStatic
    var logListener: LogListener? = object : LogListener {
        override fun onLog(priority: Int, tag: String, message: String?, throwable: Throwable?) {
            var actualMessage = message
            if (throwable != null) {
                actualMessage = message + "\n${throwable.toStackTraceString()}"
            }
            if (actualMessage == null) return

            try {
                Log.println(priority, tag, actualMessage)
            } catch (e: RuntimeException) {
                // Catch mocking issues in tests
            }
        }

    }

    @JvmStatic
    val libraryVersion = BuildConfigWrap.libraryVersionName

    @JvmStatic
    val libraryVersionFull = BuildConfigWrap.libraryVersionNameFull

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val dispatchSpy: MutableMap<String, Subject<Pair<EventDispatcher.Request, Any>>> = mutableMapOf()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val cacheSpy: MutableMap<String, HotData<*>> = mutableMapOf()

    interface LogListener {

        fun onLog(priority: Int, tag: String, message: String?, throwable: Throwable?)

    }
}