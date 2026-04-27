package de.infonline.lib.iomb.measurements.iomb.cache

import de.infonline.lib.iomb.measurements.common.caching.EventCache
import de.infonline.lib.iomb.measurements.common.processor.StandardProcessedEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

internal class IOMBEventCache @Inject internal constructor() : EventCache<StandardProcessedEvent> {

    private val eventSubject: BehaviorSubject<List<StandardProcessedEvent>> =
        BehaviorSubject.createDefault(
            emptyList()
        )
    private var eventQueue: ConcurrentLinkedQueue<StandardProcessedEvent> = ConcurrentLinkedQueue()

    override fun drain(minEvents: Int, maxEvents: Int): Single<List<StandardProcessedEvent>> {
        val eventList = mutableListOf<StandardProcessedEvent>()

        while(eventQueue.peek() != null) {
            eventList.add(eventQueue.poll()!!)
        }
        return Single.just(eventList)
    }

    override fun markAsSend(events: List<StandardProcessedEvent>): Single<EventCache.State<StandardProcessedEvent>> {
        events.forEach { eventQueue.remove(it) }
        return Single.just(State())
    }

    override fun events(): Observable<List<StandardProcessedEvent>> {
        return eventSubject
    }

    override fun store(events: List<StandardProcessedEvent>): Completable {
        return Completable.fromCallable {
            eventQueue.addAll(events)
            eventSubject.onNext(events)
            true
        }
    }

    override fun release(): Completable = Completable.complete()

    data class State(
        override val inQueue: List<StandardProcessedEvent> = emptyList(),
        override val inDispatch: List<StandardProcessedEvent> = emptyList()
    ) : EventCache.State<StandardProcessedEvent>
}