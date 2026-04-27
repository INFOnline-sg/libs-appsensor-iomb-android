package de.infonline.lib.iomb.plugins

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.Ignore
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest


class AppCloseTriggerTest : KotlinBaseTest() {

    val testScheduler = TestScheduler()

    @MockK lateinit var lifecycleOwner: LifecycleOwner
    lateinit var lifecycleRegistry: LifecycleRegistry

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        lifecycleRegistry = spyk(LifecycleRegistry(lifecycleOwner))
        every { lifecycleOwner.lifecycle } returns lifecycleRegistry

        RxAndroidPlugins.setInitMainThreadSchedulerHandler { testScheduler }
        RxAndroidPlugins.setMainThreadSchedulerHandler { testScheduler }
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createPlugin(): MeasurementPlugin = AppCloseTrigger(
            scheduler = testScheduler,
            lifecycleOwner = lifecycleOwner
    )

    @Test
    fun `downstream subscription registeres and unregisters the lifecycle observer`() {
        val plugin = createPlugin()

        val eventSub = plugin.events.subscribeOn(testScheduler).test()
        testScheduler.triggerActions()
        eventSub.assertNotComplete().assertNoValues()

        val cycleObsSlot = slot<LifecycleObserver>()
        verify { lifecycleRegistry.addObserver(capture(cycleObsSlot)) }

        eventSub.assertNotComplete().dispose()
        testScheduler.triggerActions()

        verify { lifecycleRegistry.removeObserver(cycleObsSlot.captured) }
    }

    @Test
    fun `subscription is shared and only a single lifecycle observer is registered`() {
        val plugin = createPlugin()

        val eventSub1 = plugin.events.subscribeOn(testScheduler).test()
        val eventSub2 = plugin.events.subscribeOn(testScheduler).test()
        testScheduler.triggerActions()
        eventSub1.assertNotComplete().assertNoValues()
        eventSub2.assertNotComplete().assertNoValues()

        val cycleObsSlot = slot<LifecycleObserver>()
        verify(exactly = 1) { lifecycleRegistry.addObserver(capture(cycleObsSlot)) }

        eventSub1.dispose()
        eventSub1.isDisposed shouldBe true

        eventSub2.assertNotComplete()
        testScheduler.triggerActions()

        eventSub2.dispose()
        eventSub2.isDisposed shouldBe true
        testScheduler.triggerActions()

        verify(exactly = 1) { lifecycleRegistry.removeObserver(cycleObsSlot.captured) }
    }

    @Test
    @Ignore
    fun `lifecycle events cause event emissions`() {
        val plugin = createPlugin()

        val eventSub = plugin.events.subscribeOn(testScheduler).test()
        testScheduler.triggerActions()
        eventSub.assertNotComplete().assertNoValues()

        val cycleObsSlot = slot<LifecycleObserver>()
        verify { lifecycleRegistry.addObserver(capture(cycleObsSlot)) }

        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        testScheduler.triggerActions()
        eventSub.assertValueCount(0)

        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        testScheduler.triggerActions()
        eventSub.assertValueCount(0)

        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        testScheduler.triggerActions()

        eventSub.awaitCount(1).assertNotComplete().assertValue {
            it is MeasurementPlugin.Event.Dispatch && it.forcedDispatch
        }

        eventSub.dispose()
        testScheduler.triggerActions()
        eventSub.assertValueCount(1)

        verify { lifecycleRegistry.removeObserver(cycleObsSlot.captured) }
    }

}