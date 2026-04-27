package de.infonline.lib.iomb.util.rx

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal fun <T> Observable<T>.latest(): Single<T> = take(1).singleOrError()

internal fun <T> Observable<T>.withPrevious(): Observable<Pair<T?, T>> =
        this.scan(Pair<T?, T?>(null, null)) { previous, current -> Pair(previous.second, current) }
                .skip(1)
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it as Pair<T?, T>
                }

internal fun <T> Observable<T>.filterEqual(check: (old: T, new: T) -> Boolean = { it1, it2 -> it1 != it2 }): Observable<T> =
        withPrevious()
                .filter { (old, new) ->
                    return@filter if (old != null) check(old, new) else true
                }
                .map { it.second }