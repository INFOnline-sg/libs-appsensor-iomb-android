package de.infonline.lib.iomb.measurements.iomb

import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.measurements.Measurement
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.Generator

class IOMBSetupTest {
    @Test
    fun `default identifier`() {
        val setup = IOMBSetup(
            baseUrl = "eventUrl",
            offerIdentifier = "offerId"
        )
        setup.identifier shouldBe "default"
        setup.offerIdentifier shouldBe "offerId"
        setup.configServerUrl shouldBe ""
        setup.eventServerUrl shouldBe "eventUrl/base.io"
        setup.measurementKey shouldBe "iomb.default"
    }

    @Test
    fun `direct serialization`() {
        val original = IOMBSetup(
            baseUrl = "eventUrl",
            offerIdentifier = "offerId",
            hybridIdentifier = "hybridId"
        )

        val moshi = IOLCoreModule().moshi()
        val adapter = moshi.adapter(IOMBSetup::class.java).indent("    ")

        val json = adapter.toJson(original)
        json shouldBe """
        {
            "baseUrl": "eventUrl",
            "offerIdentifier": "offerId",
            "hybridIdentifier": "hybridId",
            "identifier": "${original.identifier}",
            "type": "iomb"
        }""".trimIndent()

        val restored = adapter.fromJson(json)
        restored is IOMBSetup
        restored shouldBe original
    }

    @Test
    fun `polymorph serialization`() {
        val original = IOMBSetup(
            baseUrl = "eventUrl",
            offerIdentifier = "offerId",
            hybridIdentifier = "hybridId"
        )

        val moshi = IOLCoreModule().moshi()
        val adapter = moshi.adapter(Measurement.Setup::class.java).indent("    ")

        val json = adapter.toJson(original)
        json shouldBe """
        {
            "baseUrl": "eventUrl",
            "offerIdentifier": "offerId",
            "hybridIdentifier": "hybridId",
            "identifier": "${original.identifier}",
            "type": "iomb"
        }""".trimIndent()

        val restored = adapter.fromJson(json)
        restored is IOMBSetup
        restored shouldBe original
    }

    @Test
    fun `fixed type`() {
        val original = IOMBSetup(
            baseUrl = "eventUrl",
            offerIdentifier = "offerId"
        )
        original.type shouldBe Measurement.Type.IOMB
        shouldThrow<IllegalArgumentException> {
            original.type = Measurement.Type.SZM
            Any()
        }
        original.type shouldBe Measurement.Type.IOMB
    }

    @Test
    fun `fixed identifier`() {
        val original = IOMBSetup(
            baseUrl = "eventUrl",
            offerIdentifier = "offerId"
        )
        original.identifier shouldBe Measurement.Type.IOMB.defaultIdentifier
        shouldThrow<IllegalArgumentException> {
            original.identifier = "test"
            Any()
        }
        original.identifier shouldBe Measurement.Type.IOMB.defaultIdentifier
    }

    @Test
    fun `max argument length is 255`() {
        val genOfferIdentifier = Generator.randomString(255)
        val genHybridIdentifier = Generator.randomString(255)
        val config = IOMBSetup(
            baseUrl = "",
            offerIdentifier = genOfferIdentifier,
            hybridIdentifier = genHybridIdentifier
        )
        genOfferIdentifier.length shouldBe 255
        genHybridIdentifier.length shouldBe 255

        genOfferIdentifier shouldBe config.offerIdentifier
        genHybridIdentifier shouldBe config.hybridIdentifier
    }

    @Test
    fun `offerIdentifier sanitization`() {
        shouldThrow<IllegalArgumentException> {
            IOMBSetup(
                baseUrl = "",
                offerIdentifier = Generator.randomString(300)
            )
        }
    }

    @Test
    fun `hybridIdentifier sanitization`() {
        shouldThrow<IllegalArgumentException> {
            IOMBSetup(
                baseUrl = "",
                offerIdentifier = "offerIdentifier",
                hybridIdentifier = Generator.randomString(300)
            )
        }
    }
}