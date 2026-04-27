package de.infonline.lib.iomb.measurements.iomb.processor

import android.content.Context
import de.infonline.lib.iomb.IOLDebug
import de.infonline.lib.iomb.IOLLoginEvent
import de.infonline.lib.iomb.IOLViewEvent
import de.infonline.lib.iomb.core.IOLCoreModule
import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.events.internal.IOLLifeCycleEvent
import de.infonline.lib.iomb.measurements.Measurement
import de.infonline.lib.iomb.measurements.common.ApplicationInfoBuilder
import de.infonline.lib.iomb.measurements.common.ClientInfoBuilder
import de.infonline.lib.iomb.measurements.common.LibraryInfoBuilder
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.network.CarrierInfo
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import de.infonline.lib.iomb.measurements.common.processor.EventProcessor
import de.infonline.lib.iomb.measurements.iomb.config.IOMBConfigData
import de.infonline.lib.iomb.util.HashHelper.toMD5
import de.infonline.lib.iomb.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import testhelper.testSingle
import testhelper.toFormattedJson
import java.time.Instant
import java.util.*

internal class IOMBEventProcessorTest : KotlinBaseTest() {

    val testScheduler = TestScheduler()

    @MockK lateinit var context: Context

    @MockK lateinit var defaultSetup: Measurement.Setup

    @MockK lateinit var configData: IOMBConfigData
    @MockK lateinit var remoteConfigData: IOMBConfigData.Remote
    @MockK lateinit var remoteConfiguration: ConfigData.Remote.Config


    @MockK lateinit var networkMonitor: NetworkMonitor
    @MockK lateinit var applicationInfoBuilder: ApplicationInfoBuilder
    @MockK lateinit var clientInfoBuilder: ClientInfoBuilder
    @MockK lateinit var libraryInfoBuilder: LibraryInfoBuilder
    @MockK lateinit var timeStamper: TimeStamper

    val moshi = IOLCoreModule().moshi()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        IOLDebug.debugMode = true

        every { applicationInfoBuilder.build() } returns ApplicationInfoBuilder.Info(
                packageName = "packageName",
                versionName = "1.2.3",
                versionCode = 123
        )

        val clientInfo = ClientInfoBuilder.InfoInternal(
                uuids = null,
                screen = ClientInfoBuilder.InfoInternal.Screen(
                        resolution = "test.resolution",
                        size = 1024,
                        dpi = 55,
                        screenInches = 5.0
                ),
                network = NetworkMonitor.NetworkType.WIFI,
                carrier = CarrierInfo.Info(
                        carriers = listOf(
                                CarrierInfo.Info.Carrier(name = "Telekom"),
                                CarrierInfo.Info.Carrier(name = "Vodafone")
                        )
                ),
                locale = Locale.GERMANY,
                osVersion = "Android9000",
                platform = "fingerprint",
                deviceName = "iotest5ee391701234"
        )

        every { clientInfoBuilder.build(any()) } returns Single.just(clientInfo)
        every { networkMonitor.networkState } returns BehaviorSubject.createDefault(NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType(99)))

        val libraryInfo = LibraryInfoBuilder.Info(
                libVersion = "test.libversion",
                configVersion = "test.configversion",
                offerIdentifier = "test.offerIdentifier",
                hybridIdentifier = "test.hybridIdentifier",
                customerData = "test.customerData",
                debug = true
        )
        every { libraryInfoBuilder.build(any()) } returns Single.just(libraryInfo)

        // We want to test daylight savings time should be ON for this date, 24.10.2020
        every { timeStamper.localTimeZone } returns TimeZone.getTimeZone("Europe/Berlin")
        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(1603392193514L)

        every { defaultSetup.type } returns Measurement.Type.IOMB
        every { defaultSetup.logTag(any()) } returns ""

        every { configData.isEventAllowed(any()) } returns true
        every { configData.remoteConfig } returns remoteConfigData
        every { configData.isMeasuredRegular(any()) } returns true
        every { remoteConfigData.config } returns remoteConfiguration
        every { remoteConfigData.expirationDate } returns timeStamper.nowUTC
        every { remoteConfigData.offlineMode } returns false
        every { remoteConfigData.cache?.ttl } returns 24 * 60 * 1000L
        every { remoteConfigData.configType } returns ConfigData.ConfigType.IOMB
        every { remoteConfigData.specialParameters } returns IOMBConfigData.Remote.SpecialParameters(comment = true)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createProcessor(): IOMBEventProcessor = IOMBEventProcessor(
            setup = defaultSetup,
            scheduler = testScheduler,
            moshi = moshi,
            libraryInfoBuilder = libraryInfoBuilder,
            clientInfoBuilder = clientInfoBuilder,
            timeStamper = timeStamper,
    )

    @Test
    fun `simple event processing, process one, return one`() {
        val toProcess = object : IOLBaseEvent {
            override val identifier: String = "view"
            override val state: String? = "appeared"
            override val category: String? = "Home"
            override val comment: String? = "comment"

            override fun buildParameters(context: Context): Map<String, Any> = emptyMap()
        }

        val processor = createProcessor()
        val processedEvent = processor.process(toProcess, configData).testSingle(testScheduler).single().event

        processedEvent.toFormattedJson() shouldBe """
        {
            "sv": "1.0.0",
            "di": {
                "pv": "Android9000",
                "to": "iotest5ee391701234",
                "pn": "android"
            },
            "ti": {
                "cs": "ea84c6c8efe9b8b27e4a0c8a3d8fc50b",
                "dm": true,
                "it": "sa",
                "vr": "test.libversion"
            },
            "si": {
                "ev": "view.appeared",
                "st": "test.offerIdentifier",
                "pt": "ap",
                "cn": "de",
                "co": "comment",
                "cp": "Home",
                "dc": "app"
            }
        }
        """.trimIndent().toFormattedJson()
    }

    @Test
    fun `process minimal event, no network, non PI event with custom parameters`() {
        IOLDebug.debugMode = false
        every { networkMonitor.networkState } returns BehaviorSubject.createDefault(NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType.NO_PERMISSION))

        val toProcess: IOLBaseEvent = object : IOLBaseEvent {
            override val identifier: String = "custom"
            override val state: String? = null
            override val category: String? = null
            override val comment: String? = null

            override fun buildParameters(context: Context): Map<String, Any> = mapOf(
                    "parameterKey1" to "parameterValue",
                    "parameterKey2" to mapOf(
                            "subParamKey" to true
                    ),
                    "customParameter" to mapOf(
                            "customParameter" to mapOf(
                                    "customKey" to 123.456
                            )
                    )
            )
        }
        // events without category can't be PI events
        every { configData.isPIEvent(toProcess) } returns false

        val processor = createProcessor()
        val processedEvent = processor.process(toProcess, configData).testSingle(testScheduler).single().event
        processedEvent.toFormattedJson() shouldBe """
        {
            "sv": "1.0.0",
            "di": {
                "pv": "Android9000",
                "to": "iotest5ee391701234",
                "pn": "android"
            },
            "ti": {
                "cs": "891af7970e37f2c977f3d416f1723c84",
                "dm": true,
                "it": "sa",
                "vr": "test.libversion"
            },
            "si": {
                "ev": "custom",
                "st": "test.offerIdentifier",
                "pt": "ap",
                "cn": "de",
                "cp": "Leercode_nichtzuordnungsfaehig",
                "dc": "app"
            }
        }
        """.toFormattedJson()
    }

    @Test
    fun `validate event checksum`() {
        val toProcess = object : IOLBaseEvent {
            override val identifier: String = "view"
            override val state: String? = "appeared"
            override val category: String? = "Home"
            override val comment: String? = "comment"

            override fun buildParameters(context: Context): Map<String, Any> = emptyMap()
        }

        val processor = createProcessor()
        val processedEvent = processor.process(toProcess, configData).testSingle(testScheduler).single().event

        val expectedChecksum = "ea84c6c8efe9b8b27e4a0c8a3d8fc50b"

        (processedEvent["ti"] as Map<String, String>)["cs"] shouldBe expectedChecksum
    }

    @Test
    fun `simple dispatch creation`() {
        val processedEvent1: EventProcessor.ProcessedEvent = mockk()
        every { processedEvent1.createdAt } returns Instant.now()
        val processedEvent2: EventProcessor.ProcessedEvent = mockk()
        every { processedEvent2.createdAt } returns Instant.now()
        val eventsToDispatch = listOf(processedEvent1, processedEvent2)

        val processor = createProcessor()
        val testSub = processor.createDispatchRequest(
                events = eventsToDispatch,
                configData = configData
        ).subscribeOn(testScheduler).test()
        testScheduler.triggerActions()

        val dispatchRequest = testSub.await().assertComplete().values().single()
        dispatchRequest.events.size shouldBe 2
    }

    @Test
    fun `offline mode only affects persistence flag`() {
        every { networkMonitor.networkState } returns BehaviorSubject.createDefault(NetworkMonitor.State(isOnline = true, networkType = NetworkMonitor.NetworkType(99)))
        val processor = createProcessor()

        val dropMe: IOLBaseEvent = IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared)

        run {
            every { remoteConfigData.offlineMode } returns true
            processor.process(dropMe, configData).testSingle(testScheduler).single().persist shouldBe true
        }

        run {
            every { remoteConfigData.offlineMode } returns false
            processor.process(dropMe, configData).testSingle(testScheduler).single().persist shouldBe false
        }
    }

    @Test
    fun `only process allowed events`() {
        val processor = createProcessor()

        run {
            every { configData.isMeasuredRegular(any()) } returns true
            processor.process(
                    IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared),
                    configData
            ).testSingle(testScheduler).single().event.toString().contains("appeared")
        }

        run {
            every { configData.isMeasuredRegular(any()) } returns false
            val processingSub = processor.process(
                    IOLLoginEvent(IOLLoginEvent.IOLLoginEventType.Succeeded),
                    configData).subscribeOn(testScheduler
            ).test()
            testScheduler.triggerActions()
            processingSub.assertComplete().assertValue(emptyList())
        }
    }

    @Test
    fun `dispatched events are not shuffled`() {
        val eventsToDispatch = mutableListOf<EventProcessor.ProcessedEvent>()
        for (i in 0 until 10) {
            val processedEvent: EventProcessor.ProcessedEvent = mockk()
            every { processedEvent.event } returns mapOf("$i" to i)
            every { processedEvent.createdAt } returns Instant.now()
            eventsToDispatch.add(processedEvent)
        }

        val processor = createProcessor()
        val testSub = processor.createDispatchRequest(
            events = eventsToDispatch,
            configData = configData
        ).subscribeOn(testScheduler).test()
        testScheduler.triggerActions()

        val dispatchRequest = testSub.await().assertComplete().values().single()
        dispatchRequest.events.size shouldBe 10

        eventsToDispatch.forEach {
            dispatchRequest.events.contains(it)
        }
        dispatchRequest.events shouldBe eventsToDispatch
    }

    @Test
    fun `lifecycle events have no content code`() {
        val toProcess = object : IOLBaseEvent, IOLLifeCycleEvent {
            override val identifier: String = "view"
            override val state: String? = "appeared"
            override val category: String? = null
            override val comment: String? = "comment"

            override fun buildParameters(context: Context): Map<String, Any> = emptyMap()
        }

        val processor = createProcessor()
        val processedEvent = processor.process(toProcess, configData).testSingle(testScheduler).single().event
        (processedEvent["si"] as Map<String, Any>).containsKey("cp") shouldBe false
    }

    @Test
    fun `non lifecycle events with empty content code use default content code`() {
        val toProcess = object : IOLBaseEvent {
            override val identifier: String = "view"
            override val state: String? = "appeared"
            override val category: String? = ""
            override val comment: String? = "comment"

            override fun buildParameters(context: Context): Map<String, Any> = emptyMap()
        }

        val processor = createProcessor()
        val processedEvent = processor.process(toProcess, configData).testSingle(testScheduler).single().event
        (processedEvent["si"] as Map<String, Any>)["cp"] shouldBe "Leercode_nichtzuordnungsfaehig"
    }

    @Test
    fun `non lifecycle events with null content code use default content code`() {
        val toProcess = object : IOLBaseEvent {
            override val identifier: String = "view"
            override val state: String? = "appeared"
            override val category: String? = null
            override val comment: String? = "comment"

            override fun buildParameters(context: Context): Map<String, Any> = emptyMap()
        }

        val processor = createProcessor()
        val processedEvent = processor.process(toProcess, configData).testSingle(testScheduler).single().event
        (processedEvent["si"] as Map<String, Any>)["cp"] shouldBe "Leercode_nichtzuordnungsfaehig"
    }

    @Test
    fun `comment should be set when enabled in the config`() {
        val toProcess = object : IOLBaseEvent {
            override val identifier: String = "view"
            override val state: String = "appeared"
            override val category: String? = null
            override val comment: String = "comment"

            override fun buildParameters(context: Context): Map<String, Any> = emptyMap()
        }

        every { remoteConfigData.specialParameters } returns IOMBConfigData.Remote.SpecialParameters(comment = true)

        val processor = createProcessor()
        val processedEvent = processor.process(toProcess, configData).testSingle(testScheduler).single().event
        (processedEvent["si"] as Map<String, Any>)["co"] shouldBe "comment"
    }

    @Test
    fun `comment should be null when disabled in the config`() {
        val toProcess = object : IOLBaseEvent {
            override val identifier: String = "view"
            override val state: String = "appeared"
            override val category: String? = null
            override val comment: String = "comment"

            override fun buildParameters(context: Context): Map<String, Any> = emptyMap()
        }

        every { remoteConfigData.specialParameters } returns IOMBConfigData.Remote.SpecialParameters(comment = false)

        val processor = createProcessor()
        val processedEvent = processor.process(toProcess, configData).testSingle(testScheduler).single().event
        (processedEvent["si"] as Map<String, Any>)["co"] shouldBe null
    }
}