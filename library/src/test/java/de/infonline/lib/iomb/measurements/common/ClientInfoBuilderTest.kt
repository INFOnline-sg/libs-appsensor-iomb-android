package de.infonline.lib.iomb.measurements.common

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.measurements.common.network.CarrierInfo
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Single
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import testhelper.testSingle
import java.util.*

internal class ClientInfoBuilderTest : KotlinBaseTest() {

    @MockK lateinit var defaultSetup: Measurement.Setup

    @MockK lateinit var context: Context
    @MockK lateinit var resources: Resources

    @MockK lateinit var secureSettingsRepo: SecureSettingsRepo
    @MockK lateinit var networkMonitor: NetworkMonitor
    @MockK lateinit var carrierInfo: CarrierInfo
    @MockK lateinit var platformInfos: PlatformInfos
    @MockK lateinit var proofToken: ProofToken


    @MockK lateinit var configData: ConfigData<*, *>
    @MockK lateinit var localConfig: LocalConfiguration
    @MockK lateinit var remoteConfig: ConfigData.Remote
    @MockK lateinit var configuration: ConfigData.Remote.Configuration

    val moshi = IOLCoreModule().moshi()

    private val expectedFullInfo = ClientInfoBuilder.InfoInternal(
            screen = ClientInfoBuilder.InfoInternal.Screen(
                    resolution = "1024x800",
                    size = Configuration.SCREENLAYOUT_SIZE_NORMAL,
                    dpi = 5,
                    screenInches = 5.54
            ),
            network = NetworkMonitor.NetworkType.fromInt(99),
            carrier = CarrierInfo.Info(
                    carriers = listOf(
                            CarrierInfo.Info.Carrier(name = "Telekom"),
                            CarrierInfo.Info.Carrier(name = "Vodafone")
                    )
            ),
            locale = Locale.GERMANY,
            osVersion = "Android 9000",
            platform = "fingerprint",
            deviceName = "iotest5ee391701234"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { defaultSetup.type } returns Measurement.Type.SZM
        every { defaultSetup.logTag(any()) } returns ""

        every { context.resources } returns resources
        every { resources.displayMetrics } returns DisplayMetrics().apply {
            widthPixels = 1024
            heightPixels = 800
            densityDpi = 5
            xdpi = 234f
            ydpi = 236f
        }
        every { resources.configuration } returns Configuration().apply {
            screenLayout = 338
        }

        every { carrierInfo.info } returns Single.just(
                CarrierInfo.Info(
                        carriers = listOf(
                                CarrierInfo.Info.Carrier(name = "Telekom"),
                                CarrierInfo.Info.Carrier(name = "Vodafone")
                        )
                )
        )
        every { networkMonitor.networkType } returns Single.just(NetworkMonitor.NetworkType(99))

        every { platformInfos.osVersion } returns "Android 9000"
        every { platformInfos.fingerPrint } returns "fingerprint"

        every { secureSettingsRepo.androidId } returns "test.androidId"

        every { configData.localConfig } returns localConfig

        every { configData.remoteConfig } returns remoteConfig
        every { remoteConfig.configuration } returns configuration
        every { remoteConfig.configType } returns ConfigData.ConfigType.LEGACY
        every { configuration.hashingType } returns ConfigData.Remote.Configuration.HashingType.MD5_SHA256

        every { proofToken.lookupToken() } returns "iotest5ee391701234"
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createBuilder(): ClientInfoBuilder = ClientInfoBuilder(
            setup = defaultSetup,
            context = context,
            secureSettingsRepo = secureSettingsRepo,
            networkMonitor = networkMonitor,
            platformInfos = platformInfos,
            proofToken = proofToken,
            carrierInfo = carrierInfo
    )

    @Test
    fun `build valid info object`() {
        val builder = createBuilder()

        val info = builder.build(configData).testSingle()
        info shouldBe expectedFullInfo.copy(deviceName = null)
    }

    @Test
    fun `ACSAM client info has no UUIDs but a devicename`() {
        every { defaultSetup.type } returns Measurement.Type.ACSAM
        val builder = createBuilder()

        val info = builder.build(configData).testSingle()
        info.uuids shouldBe null
        info.deviceName shouldNotBe null
        info shouldBe expectedFullInfo.copy(uuids = null)
    }

    @Test
    fun `PI only privacy setting means no UUIDs either`() {
        val builder = createBuilder()

        val info = builder.build(configData).testSingle()
        info.uuids shouldBe null
        info shouldBe expectedFullInfo.copy(uuids = null, deviceName = null)
    }


    @Test
    fun `no device ids are tracked for ACSAM`() {
        every { remoteConfig.configType } returns ConfigData.ConfigType.ACSAM

        val builder = createBuilder()

        run {
            every { configuration.hashingType } returns ConfigData.Remote.Configuration.HashingType.MD5
            val info = builder.build(configData).test().await().values().single()
            info.uuids shouldBe null
        }
    }
}