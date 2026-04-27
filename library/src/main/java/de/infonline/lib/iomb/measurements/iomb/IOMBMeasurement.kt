package de.infonline.lib.iomb.measurements.iomb

import de.infonline.lib.iomb.measurements.common.MeasurementPlugin
import de.infonline.lib.iomb.measurements.common.MultiIdentifierBuilder
import de.infonline.lib.iomb.measurements.common.ProofToken
import de.infonline.lib.iomb.measurements.common.StandardMeasurement
import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import de.infonline.lib.iomb.measurements.common.network.NetworkMonitor
import de.infonline.lib.iomb.measurements.common.processor.StandardProcessedEvent
import de.infonline.lib.iomb.measurements.iomb.cache.IOMBEventCache
import de.infonline.lib.iomb.measurements.iomb.config.IOMBConfigData
import de.infonline.lib.iomb.measurements.iomb.config.IOMBConfigManager
import de.infonline.lib.iomb.measurements.iomb.dispatch.IOMBEventDispatcher
import de.infonline.lib.iomb.measurements.iomb.processor.IOMBEventProcessor
import de.infonline.lib.iomb.util.PerMeasurement
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Inject

@PerMeasurement
internal class IOMBMeasurement @Inject internal constructor(
    override val setup: IOMBSetup,
    scheduler: Scheduler,
    configManager: IOMBConfigManager,
    eventCache: IOMBEventCache,
    eventDispatcher: IOMBEventDispatcher,
    internal val eventProcessor: IOMBEventProcessor,
    networkMonitor: NetworkMonitor,
    multiIdentifierBuilder: MultiIdentifierBuilder,
    plugins: Set<MeasurementPlugin>,
    internal val proofToken: ProofToken
) : StandardMeasurement<
        IOMBConfigData,
        StandardProcessedEvent,
        IOMBEventDispatcher.Request,
        EventDispatcher.Response>
    (
    setup = setup,
    scheduler = scheduler,
    configManager = configManager,
    eventCache = eventCache,
    dispatcher = eventDispatcher,
    eventProcessor = eventProcessor,
    networkMonitor = networkMonitor,
    multiIdentifierBuilder = multiIdentifierBuilder,
    plugins = plugins,
    proofToken = proofToken
)