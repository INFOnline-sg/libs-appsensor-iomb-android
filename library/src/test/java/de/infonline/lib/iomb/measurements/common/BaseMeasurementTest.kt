package de.infonline.lib.iomb.measurements.common

import de.infonline.lib.iomb.measurements.Measurement
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import testhelper.KotlinBaseTest

class BaseMeasurementTest : KotlinBaseTest() {

    val testScheduler = TestScheduler()

    @MockK lateinit var setup: Measurement.Setup

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

}