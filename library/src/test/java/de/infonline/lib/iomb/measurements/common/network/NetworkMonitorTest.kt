package de.infonline.lib.iomb.measurements.common.network

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import testhelper.KotlinBaseTest
import testhelper.runTest
import testhelper.testObservableFirstValue
import testhelper.testSingle

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class NetworkMonitorTest : KotlinBaseTest() {

    val testScheduler = TestScheduler()

    @MockK lateinit var context: Context
    @MockK lateinit var packageManager: PackageManager
    @MockK lateinit var connectivityManager: ConnectivityManager
    @MockK lateinit var networkInfo: NetworkInfo

    val receiverSlot = CapturingSlot<BroadcastReceiver>()

    private fun createMonitor(): NetworkMonitor = NetworkMonitor(context, testScheduler)

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { context.packageName } returns "testpkg"
        every { context.packageManager } returns packageManager
        every { context.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, any(), any()) } returns PackageManager.PERMISSION_DENIED

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetworkInfo } returns networkInfo
        every { networkInfo.isConnected } returns false

        every { context.registerReceiver(capture(receiverSlot), any()) } answers {
            receiverSlot.captured.onReceive(context, Intent())
            null
        }
        every { context.unregisterReceiver(any()) } returns Unit
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `online check with available permission`() {
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_GRANTED
        val networkMonitor = createMonitor()

        networkMonitor.isOnline.testSingle(testScheduler) shouldBe false

        verify { context.checkPermission(any(), any(), any()) }
        verify { connectivityManager.activeNetworkInfo }
    }

    @Test
    fun `online check without available permission`() {
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_DENIED

        val networkMonitor = createMonitor()
        networkMonitor.isOnline.testSingle(testScheduler) shouldBe true

        verify { context.checkPermission(any(), any(), any()) }
        verify(exactly = 0) { connectivityManager.activeNetworkInfo }
    }

    @Test
    fun `online check with error defaulting to true`() {
        every { context.checkPermission(any(), any(), any()) } throws Exception()
        val networkMonitor = createMonitor()

        networkMonitor.isOnline.testSingle(testScheduler) shouldBe true
    }

    @Test
    fun `networktype check with available permission`() {
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { networkInfo.isConnected } returns true
        every { networkInfo.type } returns ConnectivityManager.TYPE_WIFI

        val networkMonitor = createMonitor()
        networkMonitor.networkType.testSingle(testScheduler) shouldBe NetworkMonitor.NetworkType.WIFI

        verify { context.checkPermission(any(), any(), any()) }
        verify { connectivityManager.activeNetworkInfo }

        every { networkInfo.isConnected } returns false
        networkMonitor.networkType.testSingle(testScheduler) shouldBe NetworkMonitor.NetworkType.NO_NETWORK
    }

    @Test
    fun `networktype check without available permission`() {
        every { networkInfo.isConnected } returns true
        every { networkInfo.type } returns ConnectivityManager.TYPE_WIFI

        every { context.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, any(), any()) } returns PackageManager.PERMISSION_DENIED

        val networkMonitor = createMonitor()
        networkMonitor.networkType.testSingle(testScheduler) shouldBe NetworkMonitor.NetworkType.NO_PERMISSION

        verify { context.checkPermission(any(), any(), any()) }
    }

    @Test
    fun `networktype check with error defaulting to true`() {
        every { context.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { networkInfo.isConnected } throws RuntimeException()

        val networkMonitor = createMonitor()
        networkMonitor.networkType.testSingle(testScheduler) shouldBe NetworkMonitor.NetworkType.NO_PERMISSION

        verify { context.checkPermission(any(), any(), any()) }
    }

    @Test
    fun `network state subscription is shared and unregisters broadcastreceiver on dispose`() {
        val monitor = createMonitor()
        val sub1 = monitor.networkState.subscribeOn(testScheduler).test()
        val sub2 = monitor.networkState.subscribeOn(testScheduler).test()
        testScheduler.triggerActions()

        receiverSlot.isCaptured shouldBe true

        sub1.dispose()
        verify(exactly = 0) { context.unregisterReceiver(receiverSlot.captured) }

        sub2.dispose()
        verify(exactly = 1) { context.unregisterReceiver(receiverSlot.captured) }
    }

    @Test
    fun `network changes are observable`() {
        val monitor = createMonitor()
        val sub1 = monitor.networkState.subscribeOn(testScheduler).test()
        testScheduler.triggerActions()

        sub1.awaitCount(1).assertValues(
                NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.NO_PERMISSION)
        )

        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_GRANTED
        receiverSlot.captured.onReceive(context, Intent())

        sub1.awaitCount(2).assertValues(
                NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.NO_PERMISSION),
                NetworkMonitor.State(isOnline = false, networkType = NetworkMonitor.NetworkType.NO_NETWORK)
        )

        sub1.dispose()
        verify(exactly = 1) { context.unregisterReceiver(receiverSlot.captured) }
    }

    @Test
    fun `network state emites value even without network permission`() {
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_DENIED
        val networkMonitor = createMonitor()

        networkMonitor.networkState.testObservableFirstValue(testScheduler) shouldBe NetworkMonitor.State(
                isOnline = true,
                networkType = NetworkMonitor.NetworkType.NO_PERMISSION
        )

        verify { context.checkPermission(any(), any(), any()) }
        verify(exactly = 0) { connectivityManager.activeNetworkInfo }
    }

    @Test
    fun `duplicate network state emissions are filtered`() {
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_GRANTED

        val monitor = createMonitor()

        monitor.networkState.runTest(testScheduler) {

            // Trigger duplicate emission
            receiverSlot.captured.onReceive(context, Intent())

            it.awaitCount(1).assertValues(
                    NetworkMonitor.State(isOnline = false, networkType = NetworkMonitor.NetworkType.NO_NETWORK)
            )
            it.assertValueCount(1)
        }

        verify(exactly = 1) { context.unregisterReceiver(receiverSlot.captured) }
    }
}