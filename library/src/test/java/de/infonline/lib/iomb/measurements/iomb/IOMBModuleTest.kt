package de.infonline.lib.iomb.measurements.iomb

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest

class IOMBModuleTest : KotlinBaseTest() {

    fun createModule() = IOMBModule()

    @Test
    fun `plugin count should be 4`() {
        createModule().plugins(
            mockk(), mockk(), mockk()
        ).size shouldBe 3
    }
}