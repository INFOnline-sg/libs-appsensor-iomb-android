package e2e.base

import android.app.Application
import android.content.pm.ProviderInfo
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import de.infonline.lib.iomb.IOMB
import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.IOLViewEvent
import de.infonline.lib.iomb.core.IOLInitProvider
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.ConfigManager
import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import de.infonline.lib.iomb.measurements.iomb.IOMBConfig
import de.infonline.lib.iomb.measurements.iomb.IOMBMeasurement
import de.infonline.lib.iomb.measurements.iomb.IOMBSetup
import de.infonline.lib.iomb.util.BuildConfigWrap
import io.kotest.matchers.shouldBe
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import testhelper.KotlinBaseTest
import testhelper.testObservableFirstValue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@Ignore
class IOMBLiveServerTest : KotlinBaseTest() {

    // TODO: adjust test for iomb

    val testScheduler = TestScheduler()
    private val defaultSetup = IOMBSetup(
        baseUrl = "https://relay-client.iocnt.net/relay.io",
        offerIdentifier = "iamtest"
    )
    private val defaultConfig = IOMBConfig()

    @Before
    fun setup() {
//        BuildConfigWrap.isDebugBuild shouldBe true

        val application = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(application)

        val info = ProviderInfo().apply {
            authority = "my.authority"
            grantUriPermissions = true
        }
        // Init IOL
        Robolectric.buildContentProvider(IOLInitProvider::class.java).create(info)
    }

    @After
    fun teardown() {
        IOMB.getAllBlocking().forEach {
            IOMB.delete(it.key.measurementKey).blockingGet() shouldBe (it.value != null)
        }
    }

    @Test
    fun `initialization and release`() {
        IOMB.getBlocking(Measurement.Type.ACSAM) shouldBe null

        val measurement = IOMB.createBlocking(defaultSetup) as IOMBMeasurement
        measurement.localConfig.testObservableFirstValue(testScheduler) shouldBe defaultConfig
        measurement.isReleased shouldBe false
        IOMB.getBlocking(Measurement.Type.ACSAM) shouldBe measurement

        IOMB.deleteBlocking(Measurement.Type.ACSAM)
        measurement.isReleased shouldBe true
        IOMB.getBlocking(Measurement.Type.ACSAM) shouldBe null

        measurement.localConfig.testObservableFirstValue(testScheduler) shouldBe defaultConfig
    }

    @Test
    fun `dispatch one batch of events`() {
        // Dispatch spies are not available in release builds
        if (!BuildConfigWrap.isDebugBuild) return

        IOMB.getBlocking(Measurement.Type.ACSAM) shouldBe null

        val measurement = IOMB.createBlocking(defaultSetup)

        // Wait for the initial config check to be done
        measurement.remoteConfigInfo.test().apply {
            awaitCount(2)
            dispose()
        }

        val dispatchSpy =
            IOLDebug.dispatchSpy[defaultSetup.measurementKey]!!.subscribeOn(Schedulers.io()).test()

        for (i in 0 until 2) {
            measurement.logEvent(IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared))
        }

/*        IOLDebug.cacheSpy[defaultSetup.measurementKey]!!.data.testAwaitUntil {
            it as StandardEventCache.State
            it.inQueue.size == 2
        }*/

        measurement.dispatch(forced = true)

        val (request, response) = dispatchSpy.awaitCount(1).values()[0]
        request.events.size shouldBe 2

        response as EventDispatcher.Response
        response.configStatusCode shouldBe ConfigManager.Status.OK

/*        IOLDebug.cacheSpy[defaultSetup.measurementKey]!!.data.testAwaitUntil {
            it as StandardEventCache.State
            it.inQueue.isEmpty()
        }*/

        IOMB.deleteBlocking(Measurement.Type.ACSAM)
        measurement.isReleased shouldBe true
        IOMB.getBlocking(Measurement.Type.ACSAM) shouldBe null
    }

}