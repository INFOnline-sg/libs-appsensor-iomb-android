package de.infonline.lib.iomb.plugins

import de.infonline.lib.iomb.events.internal.IOLInternetConnectionEventPrivate
import de.infonline.lib.iomb.events.internal.IOLInternetConnectionEventPrivate.IOLInternetConnectionEventPrivateType
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin.Event
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest


internal class AutoNetworkTrackerTest : KotlinBaseTest() {

    val testScheduler = TestScheduler()

    @MockK lateinit var setup: Measurement.Setup
    @MockK lateinit var networkMonitor: NetworkMonitor

    val networkStatePub: BehaviorSubject<NetworkMonitor.State> = BehaviorSubject.createDefault(
            NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.WIFI)
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { networkMonitor.networkState } returns networkStatePub
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createPlugin(): MeasurementPlugin = AutoNetworkTracker(
            scheduler = testScheduler,
            networkMonitor = networkMonitor
    )

    @Test
    fun `no initial event emission`() {
        val plugin = createPlugin()
        val testSub = plugin.events.subscribeOn(testScheduler).test()
        testScheduler.triggerActions()
        testSub.assertNotComplete().assertNoValues().dispose()
    }

    @Test
    fun `no emission if data didnt change`() {
        val plugin = createPlugin()
        val testSub = plugin.events.subscribeOn(testScheduler).test()

        testScheduler.triggerActions()
        testSub.assertNotComplete().assertNoValues()

        networkStatePub.onNext(networkStatePub.value!!)
        testScheduler.triggerActions()
        testSub.assertNotComplete().assertNoValues()

        networkStatePub.onNext(networkStatePub.value!!.copy(networkType = NetworkMonitor.NetworkType.GSM))
        testScheduler.triggerActions()
        testSub.assertNotComplete().assertValueCount(1).dispose()
    }

    @Test
    fun `interface switch and connection established`() {
        networkStatePub.onNext(NetworkMonitor.State(isOnline = false, networkType = NetworkMonitor.NetworkType.NO_NETWORK))

        val plugin = createPlugin()
        val testSub = plugin.events.subscribeOn(testScheduler).test()

        testScheduler.triggerActions()
        testSub.assertNotComplete().assertNoValues()

        networkStatePub.onNext(NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.WIFI))
        testScheduler.triggerActions()
        testSub.assertNotComplete().assertValues(
                Event.Tracking(iolEvent = IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivateType.Established)),
                Event.Tracking(iolEvent = IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivateType.SwitchedInterface))
        ).dispose()
    }

    @Test
    fun `interface switch and connection lost`() {
        networkStatePub.onNext(NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.WIFI))

        val plugin = createPlugin()
        val testSub = plugin.events.subscribeOn(testScheduler).test()

        testScheduler.triggerActions()
        testSub.assertNotComplete().assertNoValues()

        networkStatePub.onNext(NetworkMonitor.State(isOnline = false, networkType = NetworkMonitor.NetworkType.GSM))
        testScheduler.triggerActions()
        testSub.assertNotComplete().assertValues(
                Event.Tracking(iolEvent = IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivateType.Lost)),
                Event.Tracking(iolEvent = IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivateType.SwitchedInterface))
        ).dispose()
    }

    @Test
    fun `NO_NETWORK is not tracked as interface switch`() {
        networkStatePub.onNext(NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.WIFI))

        val plugin = createPlugin()
        val testSub = plugin.events.subscribeOn(testScheduler).test()

        testScheduler.triggerActions()
        testSub.assertNotComplete().assertNoValues()

        networkStatePub.onNext(NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.NO_NETWORK))
        testScheduler.triggerActions()
        testSub.assertNotComplete().assertNoValues().dispose()
    }

    @Test
    fun `network changes trigger event emissions`() {
        networkStatePub.onNext(NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.WIFI))

        val plugin = createPlugin()
        val testSub = plugin.events.subscribeOn(testScheduler).test()

        testScheduler.triggerActions()
        testSub.assertNotComplete().assertNoValues()

        networkStatePub.onNext(NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.GSM))
        testScheduler.triggerActions()
        testSub.assertNotComplete().assertValues(
                Event.Tracking(iolEvent = IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivateType.SwitchedInterface))
        ).dispose()
    }

}