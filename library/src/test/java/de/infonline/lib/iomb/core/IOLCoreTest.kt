package de.infonline.lib.iomb.core

import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.MeasurementInternal
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.measurements.iomb.IOMBSetup
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import testhelper.testMaybe
import testhelper.testObservableFirstValue
import testhelper.testSingle


internal class IOLCoreTest : KotlinBaseTest() {

    private val measurementManager: MeasurementManager = mockk()
    private val testScheduler = TestScheduler()

    private val iolCore: IOLCore = IOLCore(measurementManager, testScheduler)

    @Test
    fun `create measurement`() {
        val setup: Measurement.Setup = mockk()
        every { setup.type } returns Measurement.Type.SZM
        val config: LocalConfiguration = mockk()
        every { config.type } returns Measurement.Type.SZM
        val measurement: MeasurementInternal = mockk()

        every { measurementManager.createMeasurement(setup, config) } returns Single.just(measurement)
        iolCore.createMeasurement(setup, config).testSingle(testScheduler) shouldBe measurement
        verify { measurementManager.createMeasurement(setup, config) }
    }

    @Test
    fun `create measurement type missmatch`() {
        val setup: Measurement.Setup = mockk()
        every { setup.type } returns Measurement.Type.SZM
        val config: LocalConfiguration = mockk()
        every { config.type } returns Measurement.Type.OEWA

        shouldThrow<IllegalArgumentException> { iolCore.createMeasurement(setup, config).testSingle(testScheduler) }
        verify(exactly = 0) { measurementManager.createMeasurement(setup, config) }
    }

    @Test
    fun `get measurement`() {
        val measurement: MeasurementInternal = mockk()
        every { measurement.isReleased } returns false
        every { measurementManager.managedSetups } returns BehaviorSubject.createDefault(listOf(
            MeasurementManager.ManagedSetup(IOMBSetup(baseUrl = "http://test.base.de", offerIdentifier = "offerId"), measurement)))

        iolCore.getMeasurement("iomb.default").testMaybe(testScheduler) shouldBe measurement
        iolCore.getMeasurement("szm.default").testMaybe(testScheduler) shouldBe null
        iolCore.getMeasurement("oewa.default").testMaybe(testScheduler) shouldBe null
        iolCore.getMeasurement("acsam.default").testMaybe(testScheduler) shouldBe null
    }

    @Test
    fun `delete measurement`() {
        every { measurementManager.deleteMeasurement("test") } returns Single.just(true)
        every { measurementManager.deleteMeasurement("empty") } returns Single.just(false)

        iolCore.deleteMeasurement("test").testSingle(testScheduler) shouldBe true
        iolCore.deleteMeasurement("empty").testSingle(testScheduler) shouldBe false

        verify { measurementManager.deleteMeasurement("test") }
        verify { measurementManager.deleteMeasurement("empty") }
    }

    @Test
    fun `get all measurement setups`() {
        val mockedMeasurement: MeasurementInternal = mockk()
        val managedSetup = mockk<MeasurementManager.ManagedSetup>().apply {
            every { setup } returns IOMBSetup(baseUrl = "http://test.base.de", offerIdentifier = "offerId")
            every { measurement } returns mockedMeasurement
        }
        every { measurementManager.managedSetups } returns BehaviorSubject.createDefault(listOf(managedSetup))

        iolCore.allMeasurements.testObservableFirstValue(testScheduler) shouldBe mapOf(managedSetup.setup to managedSetup.measurement)
    }

    @Test
    fun `released measurements are not returned`() {
        val mockedMeasurement: MeasurementInternal = mockk()
        every { mockedMeasurement.isReleased } returns false
        val managedSetup = mockk<MeasurementManager.ManagedSetup>().apply {
            every { setup } returns IOMBSetup(baseUrl = "http://test.base.de", offerIdentifier = "offerId")
            every { measurement } returns mockedMeasurement
        }
        every { measurementManager.managedSetups } returns BehaviorSubject.createDefault(listOf(managedSetup))

        iolCore.getMeasurement("iomb.default").testMaybe(testScheduler) shouldBe mockedMeasurement

        every { mockedMeasurement.isReleased } returns true

        iolCore.getMeasurement("szm.default").testMaybe(testScheduler) shouldBe null
        iolCore.getMeasurement("oewa.default").testMaybe(testScheduler) shouldBe null
        iolCore.getMeasurement("acsam.default").testMaybe(testScheduler) shouldBe null

        iolCore.allMeasurements.testObservableFirstValue(testScheduler) shouldBe mapOf(managedSetup.setup to managedSetup.measurement)
    }

}