package de.infonline.lib.iomb.measurements.common

import android.content.ContentResolver
import android.content.Context
import de.infonline.lib.iomb.util.BuildVersionWrap
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest

internal class ProofTokenTest : KotlinBaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var contentResolver: ContentResolver

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.contentResolver } returns contentResolver
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `go with bluetooth name if available`() {
        mockkObject(SecureSettingsWrap)
        every { SecureSettingsWrap.getString(any(), "bluetooth_name") } returns "iotest5ee391709999"
        every { SecureSettingsWrap.getString(any(), "device_name") } returns "iotest5ee391708888"
        val pf = ProofToken(context)

        pf.lookupToken() shouldBe "iotest5ee391709999"
    }

    @Test
    fun `go with device name as fallback on Android 10`() {
        mockkObject(BuildVersionWrap)
        every { BuildVersionWrap.SDK_VERSION } returns 21
        mockkObject(SecureSettingsWrap)
        every { SecureSettingsWrap.getString(any(), "device_name") } returns "iotest5ee391708888"

        val pf = ProofToken(context)
        pf.lookupToken() shouldBe null
        every { BuildVersionWrap.SDK_VERSION } returns 29
        pf.lookupToken() shouldBe "iotest5ee391708888"
    }


    @Test
    fun `errors are catched and we return null`() {
        mockkObject(BuildVersionWrap)
        every { BuildVersionWrap.SDK_VERSION } returns 21
        mockkObject(SecureSettingsWrap)
        every { SecureSettingsWrap.getString(any(), any()) } throws RuntimeException()

        val pf = ProofToken(context)
        pf.lookupToken() shouldBe null
    }

    @Test
    fun `proof token validation with bluetooth name if available`() {
        ProofToken.validateToken("iotest5ee391709999") shouldBe true
        ProofToken.validateToken("iotest1000000009999") shouldBe true // Longer (8+ digit) timestamps are allowed
        ProofToken.validateToken("5ee39170iotest9999") shouldBe false // shuffled
        ProofToken.validateToken("99995ee39170iotest") shouldBe false // reversed
        ProofToken.validateToken("iotest5ee39170999") shouldBe false // 3-digit instead of 4 digit postfix
        ProofToken.validateToken("iotest5ee39170") shouldBe false // 0-digit instead of 4 digit postfix
        ProofToken.validateToken("test5ee391709999") shouldBe false // Not beginning with iotest
        ProofToken.validateToken("5ee391709999iotest") shouldBe false // Not beginning with iotest
        ProofToken.validateToken("iotest5ee39179999") shouldBe false // Timestamp too short
        ProofToken.validateToken("iotest9999") shouldBe false // No timestamp

        ProofToken.validateToken("iotest5EE391709999") shouldBe true // Hex casing
        ProofToken.validateToken("ioTest5EE391709999") shouldBe false // prefix casing
    }
}