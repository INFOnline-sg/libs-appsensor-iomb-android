package de.infonline.lib.iomb.measurements.iomb.dispatch

import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.config.ConfigManager
import de.infonline.lib.iomb.measurements.common.processor.StandardProcessedEvent
import de.infonline.lib.iomb.measurements.iomb.IOMBConfig
import de.infonline.lib.iomb.measurements.iomb.config.IOMBConfigData
import de.infonline.lib.iomb.util.BuildConfigWrap
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.schedulers.TestScheduler
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import testhelper.testCompletable
import testhelper.testSingle
import java.util.concurrent.TimeUnit

internal class IOMBEventDispatcherTest : KotlinBaseTest() {

    val testScheduler = TestScheduler()

    lateinit var mockWebServer: MockWebServer
    lateinit var relayUrl: String

    val moshi = IOLCoreModule().moshi()

    @MockK lateinit var defaultSetup: Measurement.Setup

    val defaultRequest: IOMBEventDispatcher.Request = IOMBEventDispatcher.Request(
        events = listOf(
            StandardProcessedEvent(
                createdAt = mockk(),
                event = mapOf(
                    "identifier" to "view",
                    "state" to "appeared",
                    "timestamp" to 123456789,
                    "network" to 1,
                    "category" to "user generated string",
                    "comment" to "user generated comment"
                )
            )
        )
    )

    val defaultConfigData = IOMBConfigData(
        localConfig = IOMBConfig(),
        remoteConfig = IOMBConfigData.Remote()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockWebServer = MockWebServer()
        mockWebServer.start()

        relayUrl = mockWebServer.url("/something").toString()

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.isDebugBuild } returns false
        BuildConfigWrap.isDebugBuild shouldBe false

        every { defaultSetup.measurementKey } returns "iomb.default"
        every { defaultSetup.offerIdentifier } returns "offerIdentifier"
        every { defaultSetup.logTag(any()) } returns ""
        every { defaultSetup.eventServerUrl } returns relayUrl
        defaultSetup.eventServerUrl shouldBe relayUrl

    }

    fun createDispatcher() = IOMBEventDispatcher(
        setup = defaultSetup,
        moshi = moshi
    )

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()

        clearAllMocks()
    }


    @Test
    fun `empty dispatch request returns empty dispatch response`() {
        val dispatcher = createDispatcher()

        val emptyRequest = defaultRequest.copy(events = emptyList())
        val dispatchResult =
            dispatcher.dispatch(emptyRequest, defaultConfigData).testSingle(testScheduler)

        mockWebServer.takeRequest(5, TimeUnit.SECONDS) shouldBe null

        dispatchResult.configStatusCode shouldBe ConfigManager.Status.OK
    }

    @Test
    fun `cache spy is only created on debug builds`() {
        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.isDebugBuild } returns false

        IOLDebug.dispatchSpy.isEmpty() shouldBe true
        val dispatcher = createDispatcher()

        IOLDebug.dispatchSpy.isEmpty() shouldBe true
        IOLDebug.dispatchSpy[defaultSetup.measurementKey] shouldBe null

        dispatcher.release().testCompletable(testScheduler)
    }

    @Test
    fun `cache spy is created on init and cleaned up on release`() {
        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.isDebugBuild } returns true

        IOLDebug.dispatchSpy.isEmpty() shouldBe true
        val dispatcher = createDispatcher()

        IOLDebug.dispatchSpy.isEmpty() shouldBe false
        IOLDebug.dispatchSpy[defaultSetup.measurementKey] shouldNotBe null

        dispatcher.release().testCompletable(testScheduler)

        IOLDebug.dispatchSpy.isEmpty() shouldBe true
        IOLDebug.dispatchSpy[defaultSetup.measurementKey] shouldBe null
    }
}