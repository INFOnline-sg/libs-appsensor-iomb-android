package de.infonline.lib.iomb.util.okio.base64

import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ForwardingSource
import okio.Source

// Based on https://github.com/erickok/okio-extensions
class Base64Source(source: Source) : ForwardingSource(source) {

    private val sourceBuffer: Buffer = Buffer()
    private val decodeBuffer: Buffer = Buffer()

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (byteCount > MAX_REQUEST_LENGTH) throw IllegalArgumentException("Invalid byteCount requested: $byteCount")

        // If we have the requested bytes already buffered, return directly
        if (decodeBuffer.size >= byteCount) {
            sink.write(decodeBuffer, byteCount)
            return byteCount
        }

        var streamEnded = false
        while (decodeBuffer.size < byteCount && !streamEnded) {
            val bytesRead = super.read(sourceBuffer, byteCount)
            if (bytesRead < 0) streamEnded = true

            val availableBlocks = BASE64_BLOCK * (sourceBuffer.size / BASE64_BLOCK)
            val decoded: ByteString = sourceBuffer.readUtf8(availableBlocks).decodeBase64()
                    ?: throw IllegalStateException("Decoding failed")
            decodeBuffer.write(decoded)
        }

        val availableBytes = byteCount.coerceAtMost(decodeBuffer.size)
        sink.write(decodeBuffer, availableBytes)

        return if (streamEnded) -1 else availableBytes
    }

    companion object {
        const val MAX_REQUEST_LENGTH = 9223372036854775804L // 4 * (Long.MAX_VALUE / 4)
        const val BASE64_BLOCK = 4 // Read blocks of 4 bytes, which fix neatly 3 decoded bytes
    }

}

fun Source.base64(): Base64Source = Base64Source(this)