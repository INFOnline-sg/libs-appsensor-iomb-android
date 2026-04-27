package de.infonline.lib.iomb.util.extensions

import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest

class StringExtensionsTest : KotlinBaseTest() {

    @Test
    fun `test gzip and gunzip`() {
        val testData = "The Cake Is A Lie"
        val compressed = testData.gzip()
        val comrpessedBase64 = compressed.base64()
        comrpessedBase64 shouldBe "H4sIAAAAAAAAAAvJSFVwTsxOVfAsVnBU8MlMBQB2wz2qEQAAAA=="
        val base64ByteString = comrpessedBase64.decodeBase64()
        val uncompressed = base64ByteString!!.gunzip()
        uncompressed shouldBe testData
    }

    val goodLengthAndChars = """1234567890ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstuvwxyz,/_-...............................................................n..............t..............................................................................................................."""
    val badLengthAndChars = """1234567890ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstuvwxyz,/_-.........................!@#${'$'}%ˆ*()=?><|\\][{}\"'˜`±¡™£¢∞§¶•ªº–\n
            \t  ......................................................................................................................................................."""

    @Test
    fun `santize string with defaults`() {
        badLengthAndChars.sanitize() shouldBe goodLengthAndChars
    }

    @Test
    fun `santize string for length only`() {
        badLengthAndChars.sanitize(disallowedChars = null) shouldBe badLengthAndChars.substring(0, 255)
    }

    @Test
    fun `santize string for chars only`() {
        """!@#%ˆ*()=?>"'˜`±¡™£¢∞§¶•ªº–\n""".sanitize(disallowedChars = null, maxLength = 5) shouldBe """!@#%ˆ"""
    }

    @Test
    fun `bytearray to bytestring conversion`() {
        val input = "The Cake is a Lie!"
        input.toByteArray().toByteString().string(Charsets.UTF_8) shouldBe input
    }
}