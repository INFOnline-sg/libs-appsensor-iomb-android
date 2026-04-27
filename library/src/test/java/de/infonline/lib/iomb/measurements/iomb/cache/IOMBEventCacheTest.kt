package de.infonline.lib.iomb.measurements.iomb.cache

import android.content.Context
import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.processor.StandardProcessedEvent
import de.infonline.lib.iomb.util.BuildConfigWrap
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.*
import java.time.Instant

internal class IOMBEventCacheTest : KotlinBaseTest() {

    @MockK lateinit var setup: Measurement.Setup
    @MockK lateinit var context: Context

    val testEvent1 = StandardProcessedEvent(
        createdAt = Instant.ofEpochMilli(100000),
        persist = true,
        event = mapOf(
            "identifier" to "view",
            "timestamp" to 1580601.645,
            "network" to 99
        )
    )
    val storedTestEvent1 = """
            {
                "version": 1,
                "inQueue": "H4sIAAAAAAAAAA2LQQrCQAxF75L1KAm01cnOO7hSXBT7hSCtZSZtF6V3N/AX78H7z53eBb1juDkpSb7wiSV2Z1YWbfhBiWaUajUCLwsSYcUUspMNAfYxlPiuhi1atxHV+3EmlfbKHcu5a9pEE3z7lS9pzsfx+gOLWSS3eAAAAA==",
                "inDispatch": "H4sIAAAAAAAAAIuOBQApu0wNAgAAAA=="
            }
        """.trimIndent()

    val testScheduler = TestScheduler()
    val moshi = IOLCoreModule().moshi()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createEventCache(): IOMBEventCache = IOMBEventCache()

    @Test
    fun `drain always returns one event`() {
        val cache = createEventCache()
        cache.events().testObservableFirstValue(testScheduler) shouldBe emptyList()

        val testEvent2 = testEvent1.copy(createdAt = Instant.ofEpochMilli(20000))
        val testEvent3 = testEvent1.copy(createdAt = Instant.ofEpochMilli(30000))

        val testEvents = listOf(testEvent1, testEvent2, testEvent3)
        val storeSub = cache.store(testEvents).subscribeOn(testScheduler).test()

        testScheduler.triggerActions()
        storeSub.await().assertComplete()

        val drainSub = cache.drain().subscribeOn(testScheduler).test()
        testScheduler.triggerActions()

        drainSub.await().assertComplete().assertValues(listOf(testEvent1))

        cache.release().testCompletable(testScheduler)
    }


    @Test
    fun `cache spy is not created`() {
        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.isDebugBuild } returns true

        IOLDebug.cacheSpy.isEmpty() shouldBe true
        val cache = createEventCache()

        IOLDebug.cacheSpy.isEmpty() shouldBe true
    }
}