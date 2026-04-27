package de.infonline.lib.iomb.measurements.iomb

import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.measurements.common.MeasurementPlugin
import de.infonline.lib.iomb.measurements.common.MultiIdentifierBuilder
import de.infonline.lib.iomb.measurements.common.ProofToken
import de.infonline.lib.iomb.measurements.common.StandardMeasurement
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.config.LocalConfiguration
import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import de.infonline.lib.iomb.measurements.common.processor.StandardProcessedEvent
import de.infonline.lib.iomb.measurements.iomb.cache.IOMBEventCache
import de.infonline.lib.iomb.measurements.iomb.config.IOMBConfigData
import de.infonline.lib.iomb.measurements.iomb.config.IOMBConfigManager
import de.infonline.lib.iomb.measurements.iomb.dispatch.IOMBEventDispatcher
import de.infonline.lib.iomb.measurements.iomb.processor.IOMBEventProcessor
import io.kotest.matchers.instanceOf
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import testhelper.testCompletable
import testhelper.testObservableFirstValue

internal class IOMBMeasurementTest : KotlinBaseTest() {

    /**
     * This is very much like the StandardMeasurementTest, but we can't merge them as we can't mockk interfaces with generics.
     * On the upside, this allows us to diverge IOMB from the StandardMeasurement flow, if required, with less need for refactoring tests.
     */

    val setup: IOMBSetup = IOMBSetup(
        baseUrl = "event-api-url",
        offerIdentifier = "offerId"
    )
    @MockK lateinit var config: IOMBConfig

    @MockK lateinit var configManager: IOMBConfigManager
    @MockK lateinit var configData: IOMBConfigData
    @MockK lateinit var remoteConfigData: IOMBConfigData.Remote
    @MockK lateinit var remoteConfiguration: ConfigData.Remote.Config
    lateinit var configPub: BehaviorSubject<IOMBConfigData>

    @MockK lateinit var eventCache: IOMBEventCache
    lateinit var eventCachePub: BehaviorSubject<List<StandardProcessedEvent>>

    @MockK lateinit var eventDispatcher: IOMBEventDispatcher
    @MockK lateinit var eventProcessor: IOMBEventProcessor
    @MockK lateinit var proofToken: ProofToken

    @MockK lateinit var multiIdentifierBuilder: MultiIdentifierBuilder

    @MockK lateinit var networkMonitor: NetworkMonitor


    internal val testScheduler = TestScheduler()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        configPub = BehaviorSubject.createDefault(configData)
        every { configManager.configuration() } returns configPub
        every { configManager.checkRemoteConfig(any()) } returns Single.just(remoteConfigData)
        every { configManager.tryUpdateRemoteConfig() } returns Single.just(remoteConfigData)

        every { configData.remoteConfig } returns remoteConfigData
        every { configData.isMeasuredRegular(any()) } returns true
        every { remoteConfigData.config } returns remoteConfiguration
        every { remoteConfigData.configType } returns ConfigData.ConfigType.IOMB
        every { remoteConfigData.getBatchSize() } returns 1
        every { remoteConfigData.sendAutoEvents } returns IOMBConfigData.Remote.SendAutoEvents(
            regular = true,
            audit = false
        )

        every { proofToken.lookupToken() } returns null

        eventCachePub = BehaviorSubject.createDefault(emptyList())
        every { eventCache.events() } returns eventCachePub
        every { eventCache.drain(minEvents = any(), maxEvents = any()) } returns Single.just(
            emptyList()
        )
        every { eventCache.store(any()) } returns Completable.complete()
        every { eventCache.release() } returns Completable.complete()
        every { eventCache.markAsSend(any()) } returns Single.just(mockk())

        val dispatchResponse: IOMBEventDispatcher.Response = mockk(relaxed = true)

        every { eventDispatcher.dispatch(any(), any()) } returns Single.just(dispatchResponse)
        every { eventDispatcher.release() } returns Completable.complete()

        every { networkMonitor.isOnline } returns Single.just(true)

        every { eventProcessor.createDispatchRequest(any(), any()) } returns Single.just(mockk())
        every { eventProcessor.release() } returns Completable.complete()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    fun createMeasurement(plugins: Set<MeasurementPlugin> = emptySet()): StandardMeasurement<
            IOMBConfigData,
            StandardProcessedEvent,
            IOMBEventDispatcher.Request,
            EventDispatcher.Response
            > = IOMBMeasurement(
        setup = setup,
        scheduler = testScheduler,
        configManager = configManager,
        eventCache = eventCache,
        eventDispatcher = eventDispatcher,
        eventProcessor = eventProcessor,
        networkMonitor = networkMonitor,
        multiIdentifierBuilder = multiIdentifierBuilder,
        plugins = plugins,
        proofToken = proofToken
    )

    @Test
    fun `dispatch on init`() {
        eventCachePub.onNext(listOf(mockk()))

        val measurement = createMeasurement()

        testScheduler.triggerActions()

        verify(exactly = 1, timeout = 5000) {
            eventCache.drain(
                minEvents = any(),
                maxEvents = any()
            )
        }

        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `event submission`() {
        val measurement = createMeasurement()

        val testEvent: IOLBaseEvent = mockk()
        val processedEvent: StandardProcessedEvent = mockk()
        every {
            eventProcessor.process(
                testEvent,
                any()
            )
        } returns Single.just(listOf(processedEvent))

        measurement.logEvent(testEvent)
        measurement.logEvent(testEvent)
        testScheduler.triggerActions()

        verify(exactly = 2) { eventProcessor.process(testEvent, any()) }
        verify(exactly = 2) { eventCache.store(listOf(processedEvent)) }
        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `cache change causes dispatch`() {
        val response: IOMBEventDispatcher.Response = mockk(relaxed = true)
        every { eventDispatcher.dispatch(any(), any()) } returns Single.just(response)

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 1) { eventCache.drain(minEvents = any(), maxEvents = any()) }
        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `dispatch mechanism`() {
        val processedEvent1: StandardProcessedEvent = mockk()
        val response: IOMBEventDispatcher.Response = mockk(relaxed = true)
        every { eventDispatcher.dispatch(any(), any()) } returns Single.just(response)
        every { eventCache.drain(minEvents = 1, maxEvents = any()) } returns Single.just(
            listOf(
                processedEvent1
            )
        )

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 1) { networkMonitor.isOnline }
        verify(exactly = 1) { eventCache.drain(minEvents = any(), maxEvents = any()) }
        verify(exactly = 1) { eventProcessor.createDispatchRequest(listOf(processedEvent1), any()) }
        verify(exactly = 1) { eventDispatcher.dispatch(any(), any()) }
        verify(exactly = 1) { eventCache.markAsSend(listOf(processedEvent1)) }
        verify(exactly = 0) { eventCache.store(any()) }
        verify(exactly = 1) { configManager.checkRemoteConfig(response) }
        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `dispatch mechanism marks all drained events as send even filtered ones`() {
        val processedEvent1: StandardProcessedEvent = mockk()
        val processedEvent2: StandardProcessedEvent = mockk()
        val response: IOMBEventDispatcher.Response = mockk(relaxed = true)
        every { eventDispatcher.dispatch(any(), any()) } returns Single.just(response)
        every { eventCache.drain(minEvents = any(), maxEvents = any()) } returns Single.just(
            listOf(
                processedEvent1,
                processedEvent2
            )
        )

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 1) { networkMonitor.isOnline }
        verify(exactly = 1) { eventCache.drain(minEvents = any(), maxEvents = any()) }
        verify(exactly = 1) {
            eventProcessor.createDispatchRequest(
                listOf(
                    processedEvent1,
                    processedEvent2
                ), any()
            )
        }
        verify(exactly = 1) { eventDispatcher.dispatch(any(), any()) }
        verify(exactly = 1) { eventCache.markAsSend(listOf(processedEvent1, processedEvent2)) }
        verify(exactly = 0) { eventCache.store(any()) }
        verify(exactly = 1) { configManager.checkRemoteConfig(response) }
        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `dispatch mechanism honors minimum bulk size`() {
        val processedEvent1: StandardProcessedEvent = mockk()
        val processedEvent2: StandardProcessedEvent = mockk()
        val response: IOMBEventDispatcher.Response = mockk(relaxed = true)

        every { eventDispatcher.dispatch(any(), any()) } returns Single.just(response)

        every { eventCache.drain(minEvents = any(), maxEvents = any()) } returns Single.just(
            listOf(
                processedEvent1
            )
        )
        every { remoteConfigData.getBatchSize() } returns 2

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 1) { eventCache.drain(minEvents = 2, maxEvents = any()) }
        verify(exactly = 0) { eventProcessor.createDispatchRequest(any(), any()) }

        every { eventCache.drain(minEvents = any(), maxEvents = any()) } returns Single.just(
            listOf(
                processedEvent1,
                processedEvent2
            )
        )

        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 2) { eventCache.drain(minEvents = 2, maxEvents = any()) }
        verify(exactly = 1) {
            eventProcessor.createDispatchRequest(
                listOf(
                    processedEvent1,
                    processedEvent2
                ), any()
            )
        }

        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `dispatch mechanism with no events to send`() {
        every { eventCache.drain() } returns Single.just(emptyList())

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 2) { eventCache.drain(minEvents = any(), maxEvents = any()) }
        verify(exactly = 0) { eventDispatcher.dispatch(any(), any()) }
        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `dispatch mechanism while offline`() {
        every { networkMonitor.isOnline } returns Single.just(false)

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 2) { networkMonitor.isOnline }
        verify(exactly = 0) { eventCache.drain() }
        verify(exactly = 0) { eventDispatcher.dispatch(any(), any()) }
        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `recover crash during cache draining`() {
        every { eventCache.drain(minEvents = any(), maxEvents = any()) } returns Single.error(
            IllegalStateException()
        )

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 0) { eventProcessor.createDispatchRequest(any(), any()) }
        verify(exactly = 0) { eventDispatcher.dispatch(any(), any()) }
        verify(exactly = 0) { eventCache.markAsSend(any()) }
        verify(exactly = 0) { configManager.checkRemoteConfig(any()) }
        measurement.lastDispatchError shouldBe instanceOf(IllegalStateException::class)
    }

    @Test
    fun `recover crash during dispatch request creation`() {
        every { eventCache.drain(any(), any()) } returns Single.just(listOf(mockk()))
        every { eventProcessor.createDispatchRequest(any(), any()) } returns Single.error(
            IllegalStateException()
        )

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 0) { eventDispatcher.dispatch(any(), any()) }
        verify(exactly = 0) { eventCache.markAsSend(any()) }
        verify(exactly = 0) { configManager.checkRemoteConfig(any()) }
        measurement.lastDispatchError shouldBe instanceOf(IllegalStateException::class)
    }

    @Test
    fun `recover crash during dispatch`() {
        every {
            eventDispatcher.dispatch(
                any(),
                any()
            )
        } returns Single.error(IllegalStateException())
        every { eventCache.drain(any(), any()) } returns Single.just(listOf(mockk()))

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 0) { eventCache.markAsSend(any()) }
        verify(exactly = 0) { configManager.checkRemoteConfig(any()) }
        measurement.lastDispatchError shouldBe instanceOf(IllegalStateException::class)
    }

    @Test
    fun `local config update`() {
        val mockConfig: IOMBConfigData = mockk()
        every { configManager.updateLocalConfig(any()) } returns Single.just(mockConfig)

        val measurement = createMeasurement()

        val slotConfigUpdate = slot<(LocalConfiguration) -> LocalConfiguration>()
        val oldConfig: LocalConfiguration = mockk()
        val newConfig: LocalConfiguration = mockk()
        measurement.updateConfig { newConfig }

        verify { configManager.updateLocalConfig(capture(slotConfigUpdate)) }
        slotConfigUpdate.captured(oldConfig) shouldBe newConfig
    }

    @Test
    fun `config update triggers dispatch`() {
        val measurement = createMeasurement()
        testScheduler.triggerActions()
        verify(exactly = 0) { eventCache.drain(minEvents = any(), maxEvents = any()) }

        configPub.onNext(configData)
        testScheduler.triggerActions()
        verify(exactly = 1) { eventCache.drain(minEvents = any(), maxEvents = any()) }
        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `data release`() {
        val testEvent: IOLBaseEvent = mockk()
        val processedEvent: StandardProcessedEvent = mockk()
        every {
            eventProcessor.process(
                testEvent,
                any()
            )
        } returns Single.just(listOf(processedEvent))
        var releasedProcessor = false
        every { eventProcessor.release() } returns Completable.fromCallable {
            releasedProcessor = true
            Unit
        }
        var releasedDispatcher = false
        every { eventDispatcher.release() } returns Completable.fromCallable {
            releasedDispatcher = true
            Unit
        }
        var releasedCache = false
        every { eventCache.release() } returns Completable.fromCallable {
            releasedCache = true
            Unit
        }

        val measurement = createMeasurement()

        measurement.isReleased shouldBe false

        measurement.release().testCompletable(testScheduler)
        testScheduler.triggerActions()

        measurement.logEvent(testEvent)
        measurement.logEvent(testEvent)
        testScheduler.triggerActions()

        verify(exactly = 0) { eventProcessor.process(any(), any()) }
        verify(exactly = 0) { eventDispatcher.dispatch(any(), any()) }

        verify { eventCache.release() }
        verify { eventProcessor.release() }
        verify { eventDispatcher.release() }

        releasedProcessor shouldBe true
        releasedCache shouldBe true
        releasedDispatcher shouldBe true
        measurement.lastDispatchError shouldBe null
        measurement.isReleased shouldBe true
    }

    @Test
    fun `calling release multiple times has no additional effects`() {
        var releasedProcessorCount = 0
        every { eventProcessor.release() } returns Completable.fromCallable {
            releasedProcessorCount++
            Unit
        }
        var releasedDispatcherCount = 0
        every { eventDispatcher.release() } returns Completable.fromCallable {
            releasedDispatcherCount++
            Unit
        }
        var releasedCacheCount = 0
        every { eventCache.release() } returns Completable.fromCallable {
            releasedCacheCount++
            Unit
        }

        val measurement = createMeasurement()

        measurement.isReleased shouldBe false
        measurement.release().testCompletable(testScheduler)
        measurement.isReleased shouldBe true

        measurement.release().testCompletable(testScheduler)

        releasedProcessorCount shouldBe 1
        releasedDispatcherCount shouldBe 1
        releasedCacheCount shouldBe 1
    }

    @Test
    fun `plugins are subscribed on init and disposed on release`() {
        val plugin: MeasurementPlugin = mockk()

        val subscriptions = mutableSetOf<Disposable>()
        val publisher = PublishSubject.create<MeasurementPlugin.Event>()
        every { plugin.events } returns publisher.doOnSubscribe { subscriptions.add(it) }

        val measurement = createMeasurement(setOf(plugin))
        testScheduler.triggerActions()

        publisher.hasObservers() shouldBe true
        subscriptions.forEach { it.isDisposed shouldBe false }
        subscriptions.size shouldBe 1

        measurement.release().testCompletable(testScheduler)
        testScheduler.triggerActions()

        subscriptions.forEach { it.isDisposed shouldBe true }
        subscriptions.size shouldBe 1
    }

    @Test
    fun `force dispatch triggers dispatch`() {
        val measurement = createMeasurement()
        testScheduler.triggerActions()
        verify(exactly = 0) { eventCache.drain(minEvents = any(), maxEvents = any()) }

        measurement.dispatch(forced = true)
        testScheduler.triggerActions()
        verify(exactly = 1) { eventCache.drain(minEvents = any(), maxEvents = any()) }
        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `forced dispatch circumvents minBulkEvents`() {
        val processedEvent1: StandardProcessedEvent = mockk()
        val response: IOMBEventDispatcher.Response = mockk(relaxed = true)

        every { eventDispatcher.dispatch(any(), any()) } returns Single.just(response)
        every { eventCache.drain(minEvents = any(), maxEvents = any()) } returns Single.just(
            listOf(
                processedEvent1
            )
        )
        every { remoteConfigData.getBatchSize() } returns 2

        val measurement = createMeasurement()

        measurement.dispatch(forced = false)
        testScheduler.triggerActions()

        verify(exactly = 1) { eventCache.drain(minEvents = 2, maxEvents = any()) }
        verify(exactly = 0) { eventProcessor.createDispatchRequest(any(), any()) }

        measurement.dispatch(forced = true)
        testScheduler.triggerActions()

        verify(exactly = 1) { eventCache.drain(minEvents = 1, maxEvents = any()) }
        verify(exactly = 1) { eventProcessor.createDispatchRequest(listOf(processedEvent1), any()) }
        measurement.lastDispatchError shouldBe null
    }

    @Test
    fun `multi identifier`() {
        val multiIdentifier: MultiIdentifierBuilder.Identifier = mockk()
        every { multiIdentifierBuilder.build(any()) } returns Single.just(multiIdentifier)

        val measurement = createMeasurement()

        measurement.multiIdentifier.testObservableFirstValue(testScheduler) shouldBe multiIdentifier
    }

    @Test
    fun `plugin based events are processed`() {

        val plugin: MeasurementPlugin = mockk()

        val subscriptions = mutableSetOf<Disposable>()
        val publisher = PublishSubject.create<MeasurementPlugin.Event>()
        every { plugin.events } returns publisher.doOnSubscribe { subscriptions.add(it) }

        val measurement = createMeasurement(setOf(plugin))
        testScheduler.triggerActions()

        val dispatchEvent = MeasurementPlugin.Event.Dispatch(forcedDispatch = true)
        publisher.onNext(dispatchEvent)
        testScheduler.triggerActions()

        verify(exactly = 1) { eventCache.drain(minEvents = 1, maxEvents = any()) }

        measurement.release().testCompletable(testScheduler)
        testScheduler.triggerActions()
    }

}