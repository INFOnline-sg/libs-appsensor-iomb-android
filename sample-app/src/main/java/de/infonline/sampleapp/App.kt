package de.infonline.sampleapp

import android.app.Application
import android.util.Log
import android.webkit.WebView
import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.IOLDebug.LogListener
import de.infonline.lib.iomb.IOMB
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.iomb.IOMBSetup

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        IOLDebug.debugMode = true
        IOLDebug.logListener = object : LogListener {
            override fun onLog(
                priority: Int,
                tag: String,
                message: String?,
                throwable: Throwable?
            ) {
                val logMessage =
                    throwable?.let { "$message\n${Log.getStackTraceString(it)}" } ?: "$message"
                Log.println(priority, tag, logMessage)
                logListeners.toList().forEach { it.invoke(priority, tag, message, throwable) }
            }
        }

        val setup = IOMBSetup(
            offerIdentifier = "iamtest",
            baseUrl = "https://data-ef4e2c0163.infonline.de",
        )

        IOMB.create(setup).subscribe { it ->
            IOMB_SESSION = it
        }

        // Alternatively, the session can be initialized synchronously:
        // IOMB_SESSION = IOMB.createBlocking(setup)

        WebView.setWebContentsDebuggingEnabled(true)
    }

    companion object {
        val logListeners = mutableListOf<(Int, String, String?, Throwable?) -> Unit>()
        lateinit var IOMB_SESSION: Measurement
    }
}