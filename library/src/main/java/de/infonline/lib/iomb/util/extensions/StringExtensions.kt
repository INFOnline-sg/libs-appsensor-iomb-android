package de.infonline.lib.iomb.util.extensions

import okio.*
import okio.ByteString.Companion.toByteString
import java.net.URLDecoder
import kotlin.math.min

internal fun String.decodeUrl(): String {
    return URLDecoder.decode(this, Charsets.UTF_8.name())
}

internal fun String.gzip(): ByteString {
    val buffer = Buffer()
    buffer.use { out ->
        GzipSink(out).buffer().use { bufferedSink ->
            bufferedSink.writeUtf8(this@gzip)
        }
    }
    return buffer.readByteString()
}

internal fun ByteString.gunzip(): String {
    val input = Buffer().use { it.write(this) }
    return GzipSource(input).buffer().use { it.readUtf8() }
}

internal fun String.sanitize(disallowedChars: String? = "[^\\w,/-]", maxLength: Int? = 255): String {
    var sanitizedString = this
    if (maxLength != null) {
        sanitizedString = sanitizedString.substring(0, min(sanitizedString.length, maxLength))
    }
    if (disallowedChars != null) {
        sanitizedString = sanitizedString.replace(Regex(disallowedChars), ".")
    }
    return sanitizedString
}

internal fun ByteArray.toByteString() = this.toByteString(0, this.size)