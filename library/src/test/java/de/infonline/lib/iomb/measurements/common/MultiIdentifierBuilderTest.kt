package de.infonline.lib.iomb.measurements.common

import android.content.res.Configuration
import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.network.CarrierInfo
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import java.util.*

internal class MultiIdentifierBuilderTest : KotlinBaseTest() {

    val testScheduler = TestScheduler()

    val moshi = IOLCoreModule().moshi()

    @MockK lateinit var libraryInfoBuilder: LibraryInfoBuilder
    @MockK lateinit var clientInfoBuilder: ClientInfoBuilder

    @MockK lateinit var configData: ConfigData<*, *>

    private val expectedMultiIdentifier = MultiIdentifierBuilder.Identifier(
            rawJson = "{\"library\":{\"libVersion\":\"2.1.2\",\"configVersion\":\"2017071800testing\",\"offerIdentifier\":\"iamtest\",\"customerData\":\"\\\"\\\"\\\"\",\"debug\":true},\"client\":{\"uuids\":{\"installationId\":\"37e0f73c473782c3c4bf716cbbfd9e36\",\"installationIdSHA256\":\"7cf9e9a05ea4b249a9e0ed87cbc86f5b54a0a92a6e70f6569b528365a56baff4\",\"advertisingIdentifier\":\"aa12cee4c188dc969f53cda17cea115\",\"advertisingIdentifierSHA256\":\"4e99a84d576c22295f8511fb3dae223fc8d392f522181f0bc0e54eb2522fb944\"},\"screen\":{\"resolution\":\"1080x1794\",\"dpi\":420,\"size\":2},\"language\":\"de\",\"country\":\"DE\",\"carrier\":\"o2\",\"network\":2,\"osIdentifier\":\"android\",\"osVersion\":\"10\",\"platform\":\"Google,Pixel 2,walleye,google,walleye,walleye\"}}")

    private val clientInfo = ClientInfoBuilder.InfoInternal(
            uuids = ClientInfoBuilder.InfoInternal.DeviceIdentifiers(
                    installationIdSHA256 = "7cf9e9a05ea4b249a9e0ed87cbc86f5b54a0a92a6e70f6569b528365a56baff4",
                    advertisingIdentifierSHA256 = "4e99a84d576c22295f8511fb3dae223fc8d392f522181f0bc0e54eb2522fb944",
                    installationId = "37e0f73c473782c3c4bf716cbbfd9e36",
                    advertisingIdentifier = "aa12cee4c188dc969f53cda17cea115",
                    androidId = null,
                    androidIdSHA256 = null
            ),
            screen = ClientInfoBuilder.InfoInternal.Screen(
                    resolution = "1080x1794",
                    size = Configuration.SCREENLAYOUT_SIZE_NORMAL,
                    dpi = 420,
                    screenInches = 5.0
            ),
            locale = Locale.GERMANY,
            osVersion = "10",
            platform = "Google,Pixel 2,walleye,google,walleye,walleye",
            carrier = CarrierInfo.Info(
                    carriers = listOf(
                            CarrierInfo.Info.Carrier(name = "o2"),
                            CarrierInfo.Info.Carrier(name = "Vodafone")
                    )
            ),
            network = NetworkMonitor.NetworkType.WIFI
    )

    private val libraryInfo = LibraryInfoBuilder.Info(
            configVersion = "2017071800testing",
            libVersion = "2.1.2",
            offerIdentifier = "iamtest",
            customerData = "\"\"\"",
            debug = true
    )


    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { clientInfoBuilder.build(configData) } returns Single.just(clientInfo)
        every { libraryInfoBuilder.build(configData) } returns Single.just(libraryInfo)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createBuilder(): MultiIdentifierBuilder = MultiIdentifierBuilder(
            moshi = moshi,
            libraryInfoBuilder = libraryInfoBuilder,
            clientInfoBuilder = clientInfoBuilder,
            scheduler = testScheduler
    )

    @Test
    fun `build valid Identifier object`() {
        val builder = createBuilder()

        val testSub = builder.build(configData).subscribeOn(testScheduler).test()
        testScheduler.triggerActions()
        testSub.assertComplete().assertValue(expectedMultiIdentifier)
    }

    @Test
    fun `empty json object on error`() {
        val builder = createBuilder()

        run {
            every { clientInfoBuilder.build(any()) } returns Single.error(Exception())
            val testSub = builder.build(configData).subscribeOn(testScheduler).test()
            testScheduler.triggerActions()
            testSub.assertComplete().assertValue(MultiIdentifierBuilder.Identifier("{}"))
        }

        run {
            every { clientInfoBuilder.build(configData) } returns Single.just(clientInfo)
            every { libraryInfoBuilder.build(any()) } returns Single.error(Exception())
            val testSub = builder.build(configData).subscribeOn(testScheduler).test()
            testScheduler.triggerActions()
            testSub.assertComplete().assertValue(MultiIdentifierBuilder.Identifier("{}"))
        }

        run {
            every { clientInfoBuilder.build(any()) } returns Single.error(Exception())
            every { libraryInfoBuilder.build(any()) } returns Single.error(Exception())
            val testSub = builder.build(configData).subscribeOn(testScheduler).test()
            testScheduler.triggerActions()
            testSub.assertComplete().assertValue(MultiIdentifierBuilder.Identifier("{}"))
        }
    }
}