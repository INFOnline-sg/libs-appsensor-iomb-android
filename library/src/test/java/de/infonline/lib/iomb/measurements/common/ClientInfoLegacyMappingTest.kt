package de.infonline.lib.iomb.measurements.common

import android.content.res.Configuration
import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.measurements.common.network.CarrierInfo
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import java.util.*

internal class ClientInfoLegacyMappingTest : KotlinBaseTest() {

    val moshi = IOLCoreModule().moshi()

    private val info = ClientInfoBuilder.InfoInternal(
            uuids = ClientInfoBuilder.InfoInternal.DeviceIdentifiers(
                    installationId = "5edf6fb73dcdc7377339443fe77c4d1", // Hash.md5Hash("test.installationId"),
                    installationIdSHA256 = "ef500fa955ac49b3bf3f14eb7272c156c458232336dcbec4f50f23bd3e6b2e5c", // Hash.sha256Hash("test.installationId"),
                    advertisingIdentifier = "a42611e2744211b5c9382f982a19f2b", // Hash.md5Hash("test.advertisingIdentifier"),
                    advertisingIdentifierSHA256 = "d0d67a4b42b21f3c2aaa65bced4f4df6979f050626e8506d1878b97699042934", // Hash.sha256Hash("test.advertisingIdentifier"),
                    androidId = "f8fc8f919f8268f9cc7377fc464c2d7d", // Hash.md5Hash("test.androidId"),
                    androidIdSHA256 = "5a940aa186cbaae1820176cdb8fa4065ae991fb01b42b23077d37a277ef41d7b" // Hash.sha256Hash("test.androidId")
            ),
            screen = ClientInfoBuilder.InfoInternal.Screen(
                    resolution = "1024x800",
                    size = Configuration.SCREENLAYOUT_SIZE_NORMAL,
                    dpi = 5,
                    screenInches = 5.0
            ),
            network = NetworkMonitor.NetworkType.fromInt(99),
            carrier = CarrierInfo.Info(
                    carriers = listOf(
                            CarrierInfo.Info.Carrier(name = "Telekom"),
                            CarrierInfo.Info.Carrier(name = "Vodafone")
                    )
            ),
            locale = Locale.US,
            osVersion = "Android 9000",
            platform = "fingerprint",
            deviceName = "iotest5ee391701234"
    )

    private val mappedInfo = ClientInfoLegacyMapping(
            uuids = ClientInfoLegacyMapping.DeviceIdentifiers(
                    installationId = "5edf6fb73dcdc7377339443fe77c4d1", // Hash.md5Hash("test.installationId"),
                    installationIdSHA256 = "ef500fa955ac49b3bf3f14eb7272c156c458232336dcbec4f50f23bd3e6b2e5c", // Hash.sha256Hash("test.installationId"),
                    advertisingIdentifier = "a42611e2744211b5c9382f982a19f2b", // Hash.md5Hash("test.advertisingIdentifier"),
                    advertisingIdentifierSHA256 = "d0d67a4b42b21f3c2aaa65bced4f4df6979f050626e8506d1878b97699042934", // Hash.sha256Hash("test.advertisingIdentifier"),
                    androidId = "f8fc8f919f8268f9cc7377fc464c2d7d", // Hash.md5Hash("test.androidId"),
                    androidIdSHA256 = "5a940aa186cbaae1820176cdb8fa4065ae991fb01b42b23077d37a277ef41d7b" // Hash.sha256Hash("test.androidId")
            ),
            screen = ClientInfoLegacyMapping.Screen(
                    resolution = "1024x800",
                    size = Configuration.SCREENLAYOUT_SIZE_NORMAL,
                    dpi = 5
            ),
            network = 99,
            carrier = "Telekom",
            language = "en",
            country = "US",
            osVersion = "Android 9000",
            platform = "fingerprint",
            deviceName = "iotest5ee391701234"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `mapping from info to legacy`() {
        info.toLegacyMapping() shouldBe mappedInfo
    }

    @Test
    fun `info does not include no network or no permission types`() {
        info.copy(network = NetworkMonitor.NetworkType.WIFI).toLegacyMapping().network shouldBe 2
        info.copy(network = NetworkMonitor.NetworkType.NO_NETWORK).toLegacyMapping().network shouldBe null
        info.copy(network = NetworkMonitor.NetworkType.NO_PERMISSION).toLegacyMapping().network shouldBe null
    }

    @Test
    fun `serialization with all values`() {
        val adapter = moshi.adapter(ClientInfoLegacyMapping::class.java).indent("    ")

        val rawJson = adapter.toJson(mappedInfo)
        rawJson shouldBe """
            {
                "uuids": {
                    "installationId": "5edf6fb73dcdc7377339443fe77c4d1",
                    "installationIdSHA256": "ef500fa955ac49b3bf3f14eb7272c156c458232336dcbec4f50f23bd3e6b2e5c",
                    "advertisingIdentifier": "a42611e2744211b5c9382f982a19f2b",
                    "advertisingIdentifierSHA256": "d0d67a4b42b21f3c2aaa65bced4f4df6979f050626e8506d1878b97699042934",
                    "androidId": "f8fc8f919f8268f9cc7377fc464c2d7d",
                    "androidIdSHA256": "5a940aa186cbaae1820176cdb8fa4065ae991fb01b42b23077d37a277ef41d7b"
                },
                "screen": {
                    "resolution": "1024x800",
                    "dpi": 5,
                    "size": 2
                },
                "language": "en",
                "country": "US",
                "carrier": "Telekom",
                "network": 99,
                "osIdentifier": "android",
                "osVersion": "Android 9000",
                "platform": "fingerprint",
                "deviceName": "iotest5ee391701234"
            }
            """.trimIndent()
        adapter.fromJson(rawJson) shouldBe mappedInfo
    }

    @Test
    fun `minimal info object with defaults`() {
        val adapter = moshi.adapter(ClientInfoLegacyMapping::class.java).indent("    ")

        val minimums = ClientInfoLegacyMapping(
                screen = mappedInfo.screen,
                language = "en",
                country = "US",
                osIdentifier = mappedInfo.osIdentifier,
                osVersion = mappedInfo.osVersion,
                platform = mappedInfo.platform
        )
        val rawJson = adapter.toJson(minimums)
        rawJson shouldBe """
            {
                "screen": {
                    "resolution": "1024x800",
                    "dpi": 5,
                    "size": 2
                },
                "language": "en",
                "country": "US",
                "osIdentifier": "android",
                "osVersion": "Android 9000",
                "platform": "fingerprint"
            }
            """.trimIndent()
        adapter.fromJson(rawJson) shouldBe minimums
    }

}