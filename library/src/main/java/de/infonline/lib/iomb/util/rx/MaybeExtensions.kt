package de.infonline.lib.iomb.util.rx

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

internal inline fun <T, R> Maybe<T>.flatMapSingleToMaybe(crossinline block: (T) -> Single<R>): Maybe<R> {
    return flatMap { block(it).toMaybe() }
}