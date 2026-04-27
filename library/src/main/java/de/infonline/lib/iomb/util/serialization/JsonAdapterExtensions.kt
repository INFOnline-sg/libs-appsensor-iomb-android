package de.infonline.lib.iomb.util.serialization

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import de.infonline.lib.iomb.util.IOLLog
import de.infonline.lib.iomb.util.extensions.tryMkFile
import de.infonline.lib.iomb.util.okio.base64.base64
import okio.*
import okio.ByteString.Companion.decodeBase64
import java.io.File
import java.io.IOException
import java.io.InterruptedIOException


fun <T> JsonAdapter<T>.into(value: T, output: Sink) {
    try {
        JsonWriter.of(output.buffer()).use {
            it.indent = "    "
            toJson(it, value)
        }
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            IOLLog.tag(TAG).w("into(value=%s, output=%s)", value, output)
        }
        throw e
    } finally {
        IOLLog.tag(TAG).v("into(value=%s, output=%s)", value, output)
    }
}

fun <T> JsonAdapter<T>.from(source: Source): T {
    try {

        val value = JsonReader.of(source.buffer()).use {
            return@use fromJson(it)
        }
        IOLLog.tag(TAG).v("from(source=%s): %s", source, value)
        return value ?: throw IOException("Can't read: $source")
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            IOLLog.tag(TAG).w("from(source=%s)", source)
        }
        throw e
    }
}

fun <T> JsonAdapter<T>.toBase64Gzip(value: T): String = try {
    Buffer().use { buffer ->
        (buffer as Sink).base64().gzip().buffer().use { sink ->
            toJson(sink, value)
        }
        buffer.readUtf8()
    }
} catch (e: Exception) {
    if (e !is InterruptedIOException) {
        IOLLog.tag(TAG).w("toBase64Gzip(value=%s) failed", value)
    }
    throw e
}

fun <T> JsonAdapter<T>.fromBase64Gzip(input: String): T? = try {
    val buffer = Buffer()
    buffer.write(input.decodeBase64()!!)
    (buffer as Source).gzip().buffer().use {
        this.fromJson(it)
    }
} catch (e: Exception) {
    if (e !is InterruptedIOException) {
        IOLLog.tag(TAG).w("fromBase64Gzip(input=%s) failed", input)
    }
    throw e
}


fun <T> JsonAdapter<T>.toFile(value: T, file: File) {
    try {
        if (!file.exists()) file.tryMkFile()
        file.sink().use { into(value, it) }
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            IOLLog.tag(TAG).w("toFile(value=%s, file=%s)", value, file)
        }
        throw e
    } finally {
        IOLLog.tag(TAG).v("toFile(value=%s, file=%s)", value, file)
    }
}

fun <T> JsonAdapter<T>.fromFile(file: File): T {
    try {
        if (!file.exists()) throw IOException("File doesn't exist: $file")

        val value = file.source().use { from(it) }
        IOLLog.tag(TAG).v("fromFile(file=%s): %s", file, value)
        return value ?: throw IOException("Can't read $file")
    } catch (e: Exception) {
        if (e !is InterruptedIOException) {
            IOLLog.tag(TAG).w("fromFile(file=%s)", file)
        }
        throw e
    }
}

private const val TAG = "JsonAdapterExtensions"
