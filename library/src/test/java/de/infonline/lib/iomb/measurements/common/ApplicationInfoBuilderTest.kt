package de.infonline.lib.iomb.measurements.common

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import de.infonline.lib.iomb.core.IOLCoreModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest

@Suppress("DEPRECATION")
class ApplicationInfoBuilderTest : KotlinBaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var packageManager: PackageManager

    val moshi = IOLCoreModule().moshi()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.packageManager } returns packageManager
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `build valid info object`() {
        val pkgInfo: PackageInfo = mockk()
        pkgInfo.packageName = "test.pkg"
        pkgInfo.versionName = "versionname"
        pkgInfo.versionCode = 1234
        every { context.packageName } returns "test.pkg"
        every { packageManager.getPackageInfo("test.pkg", 0) } returns pkgInfo

        val builder = ApplicationInfoBuilder(context)

        val info = builder.build()
        info.packageName shouldBe "test.pkg"
        info.versionName shouldBe "versionname"
        info.versionCode shouldBe 1234
    }

    @Test
    fun `info object without versionName`() {
        val pkgInfo: PackageInfo = mockk()
        pkgInfo.packageName = "test.pkg"
        pkgInfo.versionCode = 1234

        every { context.packageName } returns "test.pkg"
        every { packageManager.getPackageInfo("test.pkg", 0) } returns pkgInfo

        val builder = ApplicationInfoBuilder(context)

        val info = builder.build()
        info.packageName shouldBe "test.pkg"
        info.versionName shouldBe null
        info.versionCode shouldBe 1234
    }

    @Test
    fun `package manager throws error`() {
        every { context.packageName } returns "test.pkg"
        every { packageManager.getPackageInfo("test.pkg", 0) } throws PackageManager.NameNotFoundException()

        val builder = ApplicationInfoBuilder(context)

        val info = builder.build()
        info.packageName shouldBe "test.pkg"
        info.versionName shouldBe "0.0.0"
        info.versionCode shouldBe -1
    }

    @Test
    fun `serialization with all values`() {
        val info = ApplicationInfoBuilder.Info(
                packageName = "packageNameValue",
                versionName = "versionNameValue",
                versionCode = Long.MAX_VALUE
        )

        val adapter = moshi.adapter(ApplicationInfoBuilder.Info::class.java)

        val rawJson = adapter.toJson(info)
        rawJson shouldBe "{" +
                "\"package\":\"packageNameValue\"," +
                "\"versionName\":\"versionNameValue\"," +
                "\"versionCode\":9223372036854775807" +
                "}"
        adapter.fromJson(rawJson) shouldBe info
    }
}