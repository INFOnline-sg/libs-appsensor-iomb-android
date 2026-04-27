package de.infonline.lib.iomb.util

import android.util.Log
import androidx.annotation.VisibleForTesting
import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.util.extensions.toStackTraceString
import org.jetbrains.annotations.NonNls
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

internal object IOLLog {
    private const val TAG = "IOMb"
    private const val BUFFER_SIZE = 200
    private val logBuffer = mutableListOf<Pair<Long, String>>()
    private val timestampFormatter by lazy { TimestampFormatter() }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val errorLogCount = AtomicLong(0)

    fun clearLogBuffer() {
        synchronized(logBuffer) {
            errorLogCount.set(0)
            logBuffer.clear()
        }
    }

    fun getLogSnapshot(maxLines: Int): List<String> {
        val snapshot = synchronized(logBuffer) {
            logBuffer.toList()
        }

        val targetMaxLines = when {
            maxLines == 0 -> snapshot.size
            maxLines < 0 -> 0
            else -> min(maxLines, snapshot.size)
        }

        return snapshot.subList(0, targetMaxLines).map { (time, message) ->
            val formattedTime = timestampFormatter.toHumanReadable(time)
            "$formattedTime $message"
        }
    }

    @JvmStatic
    fun tag(vararg tags: String): LogCall = tag(*tags, public = false)

    @JvmStatic
    fun tag(vararg tags: String, public: Boolean = false): LogCall = object : LogCall(logTag(*tags), public) {}

    private fun logTag(vararg tags: String): String {
        val sb = StringBuilder("$TAG:")
        for (i in tags.indices) {
            sb.append(tags[i])
            if (i < tags.size - 1) sb.append(":")
        }
        return sb.toString()
    }

    private fun writeLog(
            tag: String,
            priority: Int,
            throwable: Throwable? = null,
            @NonNls message: String? = null,
            vararg args: Any? = emptyArray()
    ) {
        if (!IOLDebug.debugMode && priority < Log.ERROR) return

        val priorityPrefix = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            else -> priority.toString()
        }
        val formattedMessage = if (!args.isNullOrEmpty()) {
            message?.format(Locale.ROOT, *args)
        } else {
            message
        }

        if (BuildConfigWrap.isDebugBuild) {
            Timber.tag(tag).log(priority, throwable, formattedMessage)
        }

        var finalMessage: String? = formattedMessage

        throwable?.toStackTraceString()?.let {
            finalMessage += "\n$it"
        }

        if (formattedMessage == null && throwable == null) return

        synchronized(logBuffer) {
            if (logBuffer.size >= BUFFER_SIZE && logBuffer.size > 0) {
                logBuffer.removeAt(0)
            }
            logBuffer.add(System.currentTimeMillis() to "$priorityPrefix/$tag: $finalMessage")
        }

        IOLDebug.logListener?.onLog(priority, tag, formattedMessage, throwable)
    }

    internal abstract class LogCall(
            private val tag: String, private val isPublic: Boolean
    ) {
        fun v(t: Throwable?) = v(throwable = t)

        fun v(@NonNls message: String?, vararg args: Any?) = v(throwable = null, message = message, args = *args)

        fun v(throwable: Throwable? = null, @NonNls message: String? = null, vararg args: Any? = emptyArray()) {
            if (!isPublic && !BuildConfigWrap.isDebugBuild) return
            writeLog(tag, Log.VERBOSE, throwable, message, *args)
        }

        fun d(t: Throwable?) = d(throwable = t)

        fun d(@NonNls message: String?, vararg args: Any?) = d(throwable = null, message = message, args = *args)

        fun d(throwable: Throwable? = null, @NonNls message: String? = null, vararg args: Any? = emptyArray()) {
            if (!isPublic && !BuildConfigWrap.isDebugBuild) return
            writeLog(tag, Log.DEBUG, throwable, message, *args)
        }

        fun i(t: Throwable?) = i(throwable = t)

        fun i(@NonNls message: String?, vararg args: Any?) = i(throwable = null, message = message, args = *args)

        fun i(throwable: Throwable? = null, @NonNls message: String? = null, vararg args: Any? = emptyArray()) {
            if (!isPublic && !BuildConfigWrap.isDebugBuild) return
            writeLog(tag, Log.INFO, throwable, message, *args)
        }

        fun w(t: Throwable?) = w(throwable = t)

        fun w(@NonNls message: String?, vararg args: Any?) = w(throwable = null, message = message, args = *args)

        fun w(throwable: Throwable? = null, @NonNls message: String? = null, vararg args: Any? = emptyArray()) {
            if (!isPublic && !BuildConfigWrap.isDebugBuild) return
            writeLog(tag, Log.WARN, throwable, message, *args)
        }

        fun e(t: Throwable?) = w(throwable = t)

        fun e(@NonNls message: String?, vararg args: Any?) = e(throwable = null, message = message, args = *args)

        fun e(throwable: Throwable? = null, @NonNls message: String? = null, vararg args: Any? = emptyArray()) {
            errorLogCount.incrementAndGet()
            if (!isPublic && !BuildConfigWrap.isDebugBuild) return
            writeLog(tag, Log.ERROR, throwable, message, *args)
        }
    }
}

