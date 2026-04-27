package de.infonline.lib.iomb.measurements.common.caching

import de.infonline.lib.iomb.measurements.common.processor.EventProcessor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal interface EventCache<ProcessedEventT : EventProcessor.ProcessedEvent> {

    fun drain(minEvents: Int = -1, maxEvents: Int = -1): Single<List<ProcessedEventT>>

    fun markAsSend(events: List<ProcessedEventT>): Single<out State<out ProcessedEventT>>

    fun events(): Observable<List<ProcessedEventT>>

    fun store(events: List<ProcessedEventT>): Completable

    fun release(): Completable

    interface State<ProcessedEventT : EventProcessor.ProcessedEvent> {
        val inQueue: List<ProcessedEventT>
        val inDispatch: List<ProcessedEventT>
    }
}