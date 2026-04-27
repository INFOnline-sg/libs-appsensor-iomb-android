package de.infonline.lib.iomb.measurements.common.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import testhelper.KotlinBaseTest
import testhelper.testSingle

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class CarrierInfoTest : KotlinBaseTest() {

    val testScheduler = TestScheduler()

    @MockK lateinit var context: Context
    @MockK lateinit var packageManager: PackageManager
    @MockK lateinit var telephonyManager: TelephonyManager
    @MockK lateinit var subscriptionManager: SubscriptionManager


    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { context.packageName } returns "testpkg"
        every { context.packageManager } returns packageManager
        every { context.checkPermission(Manifest.permission.READ_PHONE_STATE, any(), any()) } returns PackageManager.PERMISSION_GRANTED

        every { context.getSystemService(Context.TELEPHONY_SERVICE) } returns telephonyManager
        every { telephonyManager.simOperatorName } returns "simOperatorName"
        every { telephonyManager.networkOperatorName } returns "networkOperatorName"

        every { context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) } returns subscriptionManager
        every { subscriptionManager.activeSubscriptionInfoList } returns emptyList()

    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = CarrierInfo(context, testScheduler)

    @Test
    fun `carrier name`() {
        val instance = createInstance()

        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = listOf(
            CarrierInfo.Info.Carrier(name = "simOperatorName")))

        every { telephonyManager.simOperatorName } returns ""
        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = listOf(
            CarrierInfo.Info.Carrier(name = "networkOperatorName")))

        every { telephonyManager.simOperatorName } returns null
        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = listOf(
            CarrierInfo.Info.Carrier(name = "networkOperatorName")))

        every { telephonyManager.simOperatorName } throws SecurityException()
        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = listOf(
            CarrierInfo.Info.Carrier(name = "networkOperatorName")))

        every { telephonyManager.networkOperatorName } returns ""
        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = emptyList())

        every { telephonyManager.networkOperatorName } returns null
        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = emptyList())

        every { telephonyManager.networkOperatorName } throws SecurityException()
        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = emptyList())

        every { telephonyManager.simOperatorName } returns "simOperatorName"
        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = listOf(
            CarrierInfo.Info.Carrier(name = "simOperatorName")))
    }

    @Test
    fun `carrier name check defaulting to null on error`() {
        every { context.getSystemService(Context.TELEPHONY_SERVICE) } throws RuntimeException()
        val instance = createInstance()

        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = emptyList())
    }

    @Test
    fun `on LOLLIPOP_MR1 or later we attempt to get carrier names for multiple sims `() {
        every { subscriptionManager.activeSubscriptionInfoList } returns listOf(
                mockk<SubscriptionInfo>().apply {
                    every { carrierName } returns "Telekom"
                },
                mockk<SubscriptionInfo>().apply {
                    every { carrierName } returns "Vodafone"
                },
                mockk<SubscriptionInfo>().apply {
                    every { carrierName } returns ""
                }
        )
        val instance = createInstance()

        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(
                carriers = listOf(
                        CarrierInfo.Info.Carrier(name = "Telekom"),
                        CarrierInfo.Info.Carrier(name = "Vodafone")
                )
        )
    }

    @Test
    fun `without READ_PHONE_STATE permission we don't check multi sim setups `() {
        every { context.checkPermission(Manifest.permission.READ_PHONE_STATE, any(), any()) } returns PackageManager.PERMISSION_DENIED
        every { subscriptionManager.activeSubscriptionInfoList } returns listOf(
                mockk<SubscriptionInfo>().apply {
                    every { carrierName } returns "Telekom"
                },
                mockk<SubscriptionInfo>().apply {
                    every { carrierName } returns "Vodafone"
                },
                mockk<SubscriptionInfo>().apply {
                    every { carrierName } returns ""
                }
        )
        val instance = createInstance()


        instance.info.testSingle(testScheduler) shouldBe CarrierInfo.Info(carriers = listOf(
            CarrierInfo.Info.Carrier(name = "simOperatorName")))
    }
}