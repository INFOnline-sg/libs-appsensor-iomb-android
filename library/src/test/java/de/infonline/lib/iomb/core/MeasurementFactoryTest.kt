package de.infonline.lib.iomb.core

import de.infonline.lib.iomb.measurements.iomb.IOMBComponent
import de.infonline.lib.iomb.measurements.iomb.IOMBConfig
import de.infonline.lib.iomb.measurements.iomb.IOMBSetup
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest

internal class MeasurementFactoryTest : KotlinBaseTest() {

    @MockK lateinit var iombComponentFactory: IOMBComponent.Factory
    @MockK lateinit var iombComponent: IOMBComponent


    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { iombComponentFactory.create(any(), any()) } returns iombComponent
        every { iombComponent.measurement } returns mockk()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createFactory(): MeasurementFactory = MeasurementFactory(
        iombComponentFactory = iombComponentFactory
    )

    @Test
    fun `creation with local config`() {
        val factory = createFactory()

        run {
            val setup = IOMBSetup(baseUrl = "https://rockabyte.com", offerIdentifier = "offerId")
            val config: IOMBConfig = mockk()
            factory.create(setup, config) shouldBe iombComponent.measurement
            verify { iombComponentFactory.create(setup, config) }
        }
    }

    @Test
    fun `creation with local config being null`() {
        val factory = createFactory()

        run {
            val setup = IOMBSetup(baseUrl = "https://rockabyte.com", offerIdentifier = "offerId")
            factory.create(setup, null) shouldBe iombComponent.measurement
            verify { iombComponentFactory.create(setup, null) }
        }
    }
}