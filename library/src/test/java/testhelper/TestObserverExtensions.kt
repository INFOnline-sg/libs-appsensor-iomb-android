package testhelper

import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler

private fun <T> TestObserver<T>.awaitSingle(): T = await().assertComplete().assertValueCount(1).values().single()

fun <T> Single<T>.testSingle(
        scheduler: TestScheduler = TestScheduler()
): T = let {
    val testSub = it.subscribeOn(scheduler).test()
    scheduler.triggerActions()
    testSub.awaitSingle()
}

fun <T> Observable<T>.testObservableFirstValue(
        scheduler: TestScheduler = TestScheduler()
): T = let {
    val testSub = it.subscribeOn(scheduler).test()
    scheduler.triggerActions()
    val firstValue = testSub.awaitCount(1).values().first()
    testSub.dispose()
    firstValue
}

private fun <T> TestObserver<T>.awaitMaybe(): T? = await().assertComplete().values().singleOrNull()


fun <T> Maybe<T>.testMaybe(
        scheduler: TestScheduler = TestScheduler()
): T? = let {
    val testSub = it.subscribeOn(scheduler).test()
    scheduler.triggerActions()
    testSub.awaitMaybe()
}

inline fun <T> Observable<T>.runTest(
        scheduler: TestScheduler = TestScheduler(),
        block: (TestObserver<T>
        ) -> Unit) {
    val testSub = subscribeOn(scheduler).test()
    scheduler.triggerActions()
    try {
        block(testSub)
    } finally {
        testSub.dispose()
    }
}

private fun <T> TestObserver<T>.awaitCompletable() = await().assertComplete()

fun Completable.testCompletable(
        scheduler: TestScheduler = TestScheduler()
) = let {
    val testSub = it.subscribeOn(scheduler).test()
    scheduler.triggerActions()
    testSub.awaitCompletable()
}

fun <T> Observable<T>.testAwaitUntil(scheduler: Scheduler = Schedulers.io(), predicate: (T) -> Boolean): List<T> =
        subscribeOn(scheduler)
                .takeUntil { predicate(it) }
                .test()
                .await()
                .values()