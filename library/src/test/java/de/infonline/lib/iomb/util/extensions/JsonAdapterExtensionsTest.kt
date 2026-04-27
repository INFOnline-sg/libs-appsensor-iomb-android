package de.infonline.lib.iomb.util.extensions

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import de.infonline.lib.iomb.util.serialization.fromBase64Gzip
import de.infonline.lib.iomb.util.serialization.toBase64Gzip
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest

class JsonAdapterExtensionsTest : KotlinBaseTest() {

    @JsonClass(generateAdapter = true)
    data class TheCake(
            val isALie: Boolean,
            val timestamp: Long = 1234567890987654321
    )

    @Test
    fun `test base64gzip`() {
        val input = TheCake(isALie = true)
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(TheCake::class.java)

        val encoded = adapter.toBase64Gzip(input)
        encoded shouldBe "H4sIAAAAAAAAAKtWyix29MlMVbIqKSpN1VEqycxNLS5JzC1QsjI0MjYxNTO3sDSwtDA3MzUxNjKsBQDX6YyALwAAAA=="
        adapter.fromBase64Gzip(encoded) shouldBe input
    }

}