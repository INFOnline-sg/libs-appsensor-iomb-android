package de.infonline.lib.iomb

import de.infonline.lib.iomb.core.IOLCore
import de.infonline.lib.iomb.core.IOLCoreComponent
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.MeasurementInternal
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import testhelper.testMaybe
import testhelper.testObservableFirstValue
import testhelper.testSingle

class IOLTest : KotlinBaseTest() {

    @MockK internal lateinit var iolCore: IOLCore

    val testScheduler = TestScheduler()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        IOMB.iolCore = iolCore
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `singleton init`() {
        IOMB.init {
            mockk<IOLCoreComponent>().apply {
                every { iolCore } returns mockk()
            }
        }
        val graph1 = IOMB.objGraph
        IOMB.init {
            mockk<IOLCoreComponent>().apply {
                every { iolCore } returns mockk()
            }
        }
        graph1 shouldBe IOMB.objGraph
    }

    @Test
    fun `create measurement`() {
        val setup: Measurement.Setup = mockk()
        val config: LocalConfiguration = mockk()
        val measurement: MeasurementInternal = mockk()
        every { iolCore.createMeasurement(setup, config) } returns Single.just(measurement)
        IOMB.createBlocking(setup) shouldBe measurement
        verify { iolCore.createMeasurement(setup, config) }
    }

    @Test
    fun `get measurement`() {
        every { iolCore.getMeasurement("testkey") } returns Maybe.empty()
        IOMB.get("testkey").testMaybe(testScheduler) shouldBe null
        verify { iolCore.getMeasurement("testkey") }

        val measurement: MeasurementInternal = mockk()
        every { iolCore.getMeasurement(Measurement.Type.SZM.defaultKey) } returns Maybe.just(measurement)
        IOMB.getBlocking(Measurement.Type.SZM) shouldBe measurement
        verify { iolCore.getMeasurement(Measurement.Type.SZM.defaultKey) }
    }

    @Test
    fun `delete measurement`() {
        every { iolCore.deleteMeasurement("1") } returns Single.just(true)
        IOMB.delete("1").testSingle(testScheduler) shouldBe true
        verify { iolCore.deleteMeasurement("1") }

        every { iolCore.deleteMeasurement(Measurement.Type.SZM.defaultKey) } returns Single.just(false)
        IOMB.deleteBlocking(Measurement.Type.SZM) shouldBe false
        verify { iolCore.deleteMeasurement(Measurement.Type.SZM.defaultKey) }
    }

    @Test
    fun `get all setups`() {
        val testData = mapOf(mockk<Measurement.Setup>() to null)
        every { iolCore.allMeasurements } returns BehaviorSubject.createDefault(testData)
        IOMB.getAllBlocking() shouldBe testData

        IOMB.getAll().testObservableFirstValue() shouldBe testData
    }

}