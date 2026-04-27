package de.infonline.lib.iomb.util

import android.util.Log
import de.infonline.lib.iomb.IOLDebug
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import testhelper.JUnitTree
import timber.log.Timber


class LogItTest {

    @BeforeEach
    fun setup() {
        Timber.plant(JUnitTree())
    }

    @AfterEach
    fun teardown() {
        Timber.uprootAll()
    }

    @Test
    fun `default logging`() {
        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.isDebugBuild } returns true

        IOLDebug.debugMode = true

        val lastCall = arrayOfNulls<Any?>(4)
        IOLDebug.logListener = object : IOLDebug.LogListener {
            override fun onLog(priority: Int, tag: String, message: String?, throwable: Throwable?) {
                lastCall[0] = priority
                lastCall[1] = tag
                lastCall[2] = message
                lastCall[3] = throwable
            }
        }

        val exception = Exception("Error message")
        val message = "Message: %s %s"
        val args = arrayOf("arg1", "arg2")

        IOLLog.tag("123", "456").v(exception, message, *args)

        lastCall[0] shouldBe Log.VERBOSE
        lastCall[1] shouldBe "IOMb:123:456"
        lastCall[2] as String shouldStartWith String.format(message, *args)
        lastCall[3] shouldBe exception
    }

    @Test
    fun `only errors if debug is not enabled`() {
        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.isDebugBuild } returns true

        IOLDebug.debugMode = true

        val lastCall = arrayOfNulls<Any?>(4)
        IOLDebug.logListener = object : IOLDebug.LogListener {
            override fun onLog(priority: Int, tag: String, message: String?, throwable: Throwable?) {
                lastCall[0] = priority
                lastCall[1] = tag
                lastCall[2] = message
                lastCall[3] = throwable
            }
        }

        val exception = Exception("Error message")
        val message = "Message: %s %s"
        val args = arrayOf("arg1", "arg2")

        IOLDebug.debugMode = false
        IOLLog.tag("123", "456").v(exception, message, *args)
        lastCall shouldBe arrayOfNulls(4)
        IOLLog.tag("123", "456").d(exception, message, *args)
        lastCall shouldBe arrayOfNulls(4)
        IOLLog.tag("123", "456").i(exception, message, *args)
        lastCall shouldBe arrayOfNulls(4)
        IOLLog.tag("123", "456").w(exception, message, *args)
        lastCall shouldBe arrayOfNulls(4)

        IOLLog.tag("123", "456").e(exception, message, *args)


        lastCall[0] shouldBe Log.ERROR
        lastCall[1] shouldBe "IOMb:123:456"
        lastCall[2] as String shouldStartWith String.format(message, *args)
        lastCall[3] shouldBe exception
    }

    @Test
    fun `only log non public messages in debug builds`() {
        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.isDebugBuild } returns true

        IOLDebug.debugMode = true

        val listener = object : IOLDebug.LogListener {
            override fun onLog(priority: Int, tag: String, message: String?, throwable: Throwable?) {

            }
        }
        val spyTree = spyk(listener)
        IOLDebug.logListener = spyTree

        val exception: Exception? = null

        val notInternalMessage = "Public message"
        IOLLog.tag("tag").v(notInternalMessage)
        IOLLog.tag("tag", public = true).v(exception, notInternalMessage)
        IOLLog.tag("tag", public = false).v(exception, notInternalMessage)
        verify(exactly = 3) { spyTree.onLog(Log.VERBOSE, "IOMb:tag", notInternalMessage, exception) }

        every { BuildConfigWrap.isDebugBuild } returns false

        val internalMessage = "Internal message!"
        IOLLog.tag("tag", public = false).v(exception, internalMessage)
        verify(exactly = 0) { spyTree.onLog(Log.VERBOSE, "IOMb:tag", internalMessage, exception) }
    }

}