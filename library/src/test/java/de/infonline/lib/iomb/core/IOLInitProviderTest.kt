package de.infonline.lib.iomb.core

import android.content.pm.ProviderInfo
import android.os.Build
import de.infonline.lib.iomb.IOMB
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import testhelper.KotlinBaseTest


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class IOLInitProviderTest : KotlinBaseTest() {

    @Test
    fun `provider requires ProviderInfo`() {
        val exception = shouldThrow<IllegalStateException> {
            Robolectric.buildContentProvider(IOLInitProvider::class.java).create(null as ProviderInfo?).get()
        }
        exception.message shouldContain "is null"
    }

    @Test
    fun `authority must match applicationId`() {
        val exception = shouldThrow<IllegalStateException> {
            Robolectric.buildContentProvider(IOLInitProvider::class.java).create(IOLInitProvider.DEFAULT_AUTHORITY).get()
        }
        exception.message shouldContain "Did you declare 'applicationId'"
    }

    @Test
    fun `successful init`() {
        val providerInfo = mockk<ProviderInfo>()
        providerInfo.authority = "com.pkg.test.core.IOLInitProvider"

        mockkObject(IOMB)

        val provider = Robolectric.buildContentProvider(IOLInitProvider::class.java).create(providerInfo).get()

        verify { IOMB.init(context = provider.context!!) }
    }

}