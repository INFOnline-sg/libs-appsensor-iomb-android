package de.infonline.lib.iomb.util.okio.base64

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import okio.Buffer
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import java.util.*

// Based on https://github.com/erickok/okio-extensions/tree/master/lib/src/main/java/nl/nl2312/okio/base64
class Base64SourceTest : KotlinBaseTest() {

    private val base64String = "b2tpbyBvaCBtecK/wqE=" // okio oh my¿¡
    private val base64StringSource = Buffer().writeUtf8(base64String)

    @Test
    fun `read from fixed string`() {
        val decoded = Base64Source(base64StringSource)

        val output = Buffer().also { it.writeAll(decoded) }
        output.readUtf8() shouldBe "okio oh my¿¡"
    }

    @Test
    fun `read from bytestring`() {
        base64String.decodeBase64()!!.string(Charsets.UTF_8) shouldBe "okio oh my¿¡"
    }

    @Test
    fun `read from random long string`() {
        // Generate a very long random string
        val randomLongByteArray =
                Random().let { random -> (0..10_000).map { (random.nextInt()).toByte() } }.toByteArray()
        val randomLongBase64 = randomLongByteArray.toByteString(0, randomLongByteArray.size)
        val randomLongSource = Buffer().also { it.writeUtf8(randomLongBase64.base64()) }

        val decoded = Base64Source(randomLongSource)

        val output = Buffer().also { it.writeAll(decoded) }
        output.readUtf8() shouldBe randomLongBase64.utf8()
    }

    @Test
    fun `partial read`() {
        val decoded = Base64Source(base64StringSource)
        val output = Buffer()

        // Request only the first 5 characters; this tests partial reading
        decoded.read(output, 5)
        output.readUtf8() shouldBe "okio "
    }

    @Test
    fun `read but stop on source end`() {
        val decoded = Base64Source(base64StringSource)
        val output = Buffer()

        // Request 1 byte at a time; this tests buffering
        do while (decoded.read(output, 1) > 0)
        output.readUtf8() shouldBe "okio oh my¿¡"

        // Trying to read more returns no more bytes
        val readMore = decoded.read(output, 1)
        readMore shouldBe -1
    }

    @Test
    fun `read overflow`() {
        val decoded = Base64Source(base64StringSource)
        val output = Buffer()

        shouldThrow<IllegalArgumentException> {
            // Request more bytes than Base64 can take due to buffer length
            decoded.read(output, Long.MAX_VALUE)
        }
    }

}