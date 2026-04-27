package de.infonline.lib.iomb.measurements.common

import android.content.Context
import android.content.pm.PackageManager
import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.measurements.iomb.IOMBSetup
import de.infonline.lib.iomb.util.BuildConfigWrap
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import kotlin.time.ExperimentalTime


@ExperimentalTime class LibraryInfoBuilderTest : KotlinBaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var packageManager: PackageManager

    val moshi = IOLCoreModule().moshi()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.packageManager } returns packageManager

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.libraryVersionName } returns "4.0.0"

        IOLDebug.debugMode = false
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `build valid info object`() {
        val configData: ConfigData<*, *> = mockk()
        val localConfig: LocalConfiguration = mockk()
        every { configData.localConfig } returns localConfig


        val configuration: ConfigData.Remote.Configuration = mockk()

        val remoteConfig: ConfigData.Remote = mockk()
        every { configData.remoteConfig } returns remoteConfig
        every { configData.remoteConfig.configuration } returns configuration
        every { configData.remoteConfig.getConfigVersion() } returns "1.2.3"

        val builder = LibraryInfoBuilder(
            IOMBSetup(
                baseUrl = "http://test.base.de",
                offerIdentifier = "testOfferIdentifier",
                hybridIdentifier = "testHybridIdentifier",
                customerData = "testCustomerData"
            )
        )

        IOLDebug.debugMode = false
        IOLDebug.debugMode shouldBe false

        val info = builder.build(configData).blockingGet()

        info.configVersion shouldBe "1.2.3"
        info.libVersion shouldBe "4.0.0"
        info.offerIdentifier shouldBe "testOfferIdentifier"
        info.hybridIdentifier shouldBe "testHybridIdentifier"
        info.customerData shouldBe "testCustomerData"
        info.debug shouldBe null

        IOLDebug.debugMode = true
        builder.build(configData).test().await().values().single().debug shouldBe true

        IOLDebug.debugMode = false
        IOLDebug.debugMode shouldBe false
        builder.build(configData).test().await().values().single().debug shouldBe null
    }

    @Test
    fun `serialization with all values`() {
        val info = LibraryInfoBuilder.Info(
            configVersion = "configVersionValue",
            libVersion = "libVersionValue",
            offerIdentifier = "offerIdentifierValue",
            hybridIdentifier = "hybridIdentifierValue",
            customerData = "customerDataValue",
            debug = true
        )

        val adapter = moshi.adapter(LibraryInfoBuilder.Info::class.java).indent("    ")
        val jsonRaw = adapter.toJson(info)
        jsonRaw shouldBe """
            {
                "libVersion": "libVersionValue",
                "configVersion": "configVersionValue",
                "offerIdentifier": "offerIdentifierValue",
                "hybridIdentifier": "hybridIdentifierValue",
                "customerData": "customerDataValue",
                "debug": true
            }
        """.trimIndent()

        adapter.fromJson(jsonRaw) shouldBe info
    }

    @Test
    fun `serialization with defaults`() {
        val info = LibraryInfoBuilder.Info(
            configVersion = "configVersionValue",
            libVersion = "libVersionValue",
            offerIdentifier = "offerIdentifierValue"
        )

        val adapter = moshi.adapter(LibraryInfoBuilder.Info::class.java).indent("    ")
        val jsonRaw = adapter.toJson(info)
        jsonRaw shouldBe """
            {
                "libVersion": "libVersionValue",
                "configVersion": "configVersionValue",
                "offerIdentifier": "offerIdentifierValue"
            }
        """.trimIndent()

        adapter.fromJson(jsonRaw) shouldBe info
    }
}