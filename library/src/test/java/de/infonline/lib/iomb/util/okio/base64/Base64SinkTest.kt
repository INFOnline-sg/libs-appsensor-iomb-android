package de.infonline.lib.iomb.util.okio.base64

import io.kotest.matchers.shouldBe
import okio.Buffer
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest

// Based on https://github.com/erickok/okio-extensions/tree/master/lib/src/main/java/nl/nl2312/okio/base64
class Base64SinkTest : KotlinBaseTest() {

    private val utf8String = "okio oh my¿¡"
    private val utf8Sink = Buffer().writeUtf8(utf8String)

    @Test
    fun `decode from static input`() {
        val output = Buffer()
        val sink = Base64Sink(output)

        sink.write(utf8Sink, Long.MAX_VALUE)

        output.readUtf8() shouldBe "b2tpbyBvaCBtecK/wqE="
    }

    @Test
    fun `decode partial`() {
        val output = Buffer()
        val sink = Base64Sink(output)

        // Request only to write the first 5 (decoded) characters
        sink.write(utf8Sink, 5)

        output.readUtf8() shouldBe "b2tpbyA="
    }

}