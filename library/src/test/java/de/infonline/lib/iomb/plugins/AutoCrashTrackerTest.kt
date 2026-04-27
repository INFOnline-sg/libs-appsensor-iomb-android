package de.infonline.lib.iomb.plugins

import android.content.Context
import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin.Event
import de.infonline.lib.iomb.util.extensions.File
import de.infonline.lib.iomb.util.extensions.deleteAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import java.io.File

class AutoCrashTrackerTest : KotlinBaseTest() {

    val testScheduler = TestScheduler()

    @MockK lateinit var context: Context
    @MockK lateinit var setup: Measurement.Setup

    val moshi = IOLCoreModule().moshi()

    val testPathDataDir = File(TEST_DIR, "LegacyEventCacheTest")
    val crashFile = File(testPathDataDir, "crashes", "crashes.json")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        testPathDataDir.deleteAll()
        testPathDataDir.mkdirs()

        every { setup.getDataDir(context) } returns testPathDataDir
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testPathDataDir.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        Thread.setDefaultUncaughtExceptionHandler(null)
    }

    private fun createPlugin(): AutoCrashTracker = AutoCrashTracker(
            setup = setup,
            scheduler = testScheduler,
            context = context,
            moshi = moshi
    )

    @Test
    fun `downstream subscription causes exception handler to be setup`() {
        val plugin = createPlugin()

        plugin.ourHandler shouldBe null

        val eventSub = plugin.events.subscribeOn(testScheduler).test()
        testScheduler.triggerActions()
        eventSub.assertNotComplete().assertNoValues()

        plugin.ourHandler shouldNotBe null

        eventSub.assertNotComplete().dispose()
        testScheduler.triggerActions()

        plugin.ourHandler shouldBe null
    }

    @Test
    fun `we wrap the default handler and pass exceptions and restore on termination`() {
        val plugin = createPlugin()

        val originalHandler: Thread.UncaughtExceptionHandler = mockk(relaxed = true)
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
        Thread.getDefaultUncaughtExceptionHandler() shouldBe originalHandler

        val eventSub = plugin.events.subscribeOn(testScheduler).test()
        testScheduler.triggerActions()
        eventSub.assertNotComplete().assertNoValues()

        val testException = IllegalStateException()
        plugin.ourHandler!!.uncaughtException(Thread.currentThread(), testException)

        crashFile.exists() shouldBe true

        Thread.getDefaultUncaughtExceptionHandler() shouldBe plugin.ourHandler
        verify { originalHandler.uncaughtException(Thread.currentThread(), testException) }

        eventSub.assertNotComplete().dispose()
        testScheduler.triggerActions()

        Thread.getDefaultUncaughtExceptionHandler() shouldBe originalHandler
    }

    @Test
    fun `uncaught exceptions trigger events`() {
        run {
            val plugin = createPlugin()

            plugin.ourHandler shouldBe null

            val eventSub = plugin.events.subscribeOn(testScheduler).test()
            testScheduler.triggerActions()
            eventSub.assertNotComplete().assertNoValues()

            crashFile.exists() shouldBe false

            plugin.ourHandler!!.uncaughtException(Thread.currentThread(), IllegalStateException())


            crashFile.exists() shouldBe true
            eventSub.assertNotComplete().assertNoValues().dispose()
        }
        run {
            val plugin = createPlugin()

            val eventSub = plugin.events.subscribeOn(testScheduler).test()
            testScheduler.triggerActions()

            eventSub.awaitCount(1).assertNotComplete().assertValue {
                it is Event.Tracking && it.iolEvent.state == "crashed"
            }.dispose()
        }
    }

}