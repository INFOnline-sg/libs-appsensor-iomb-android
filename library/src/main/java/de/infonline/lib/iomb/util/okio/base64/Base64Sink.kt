package de.infonline.lib.iomb.util.okio.base64

import okio.Buffer
import okio.ForwardingSink
import okio.Sink

// Based on https://github.com/erickok/okio-extensions
class Base64Sink(delegate: Sink) : ForwardingSink(delegate) {

    override fun write(source: Buffer, byteCount: Long) {
        val bytesToRead = byteCount.coerceAtMost(source.size)
        val decoded = source.readByteString(bytesToRead)

        val encodedSink = Buffer()
        encodedSink.writeUtf8(decoded.base64())
        super.write(encodedSink, encodedSink.size)
    }

}

fun Sink.base64(): Base64Sink = Base64Sink(this)