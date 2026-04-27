package de.infonline.lib.iomb.measurements.common.processor

import de.infonline.lib.iomb.events.IOLBaseEvent
import de.infonline.lib.iomb.measurements.common.config.ConfigData
import de.infonline.lib.iomb.measurements.common.dispatch.EventDispatcher
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.time.Instant

internal interface EventProcessor<
        ProcessedEventT : EventProcessor.ProcessedEvent,
        ConfigurationT : ConfigData<*, *>,
        RequestT : EventDispatcher.Request
        > {

    fun process(event: IOLBaseEvent, configData: ConfigurationT): Single<List<ProcessedEventT>>

    fun createDispatchRequest(events: List<ProcessedEvent>, configData: ConfigurationT): Single<out RequestT>

    fun release(): Completable

    interface ProcessedEvent {
        val createdAt: Instant
        val persist: Boolean
        val event: Map<String, Any>
    }

}