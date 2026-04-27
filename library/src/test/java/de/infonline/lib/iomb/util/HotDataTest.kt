package de.infonline.lib.iomb.util

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.SingleSubject
import org.junit.jupiter.api.Test
import testhelper.KotlinBaseTest
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class HotDataTest : KotlinBaseTest() {

    @Test
    fun `callback constructor`() {
        val callback = mockk<() -> String>()
        every { callback.invoke() } returns "testval"

        val hotData = HotData("TestThread", callback)
        hotData.snapshot shouldBe "testval"
        verify { callback.invoke() }
    }

    @Test
    fun `error while providing initial value`() {
        val callback = mockk<() -> String>()
        every { callback.invoke() } throws IllegalStateException()

        val hotData = HotData("TestThread", callback)
        hotData.data.test().awaitDone(1, TimeUnit.SECONDS)
                .assertError(IllegalStateException::class.java)
    }

    @Test
    fun `publisher constructor`() {
        val publisher = SingleSubject.create<String>()

        val hotData = HotData(publisher)
        hotData.data.timeout(1, TimeUnit.SECONDS).test().await().assertError(TimeoutException::class.java)

        publisher.onSuccess("cake")

        hotData.data.take(1).test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValue("cake")
                .assertComplete()
    }

    @Test
    fun `publisher constructor error`() {
        val publisher = SingleSubject.create<String>()

        val hotData = HotData(publisher)
        hotData.data.timeout(1, TimeUnit.SECONDS).test().await().assertError(TimeoutException::class.java)

        publisher.onError(IOException("Whoop"))

        hotData.data.test().await().assertError(IOException::class.java)
    }

    @Test
    fun `close hotdata`() {
        val testScheduler = TestScheduler()
        val hotData = HotData(Single.just("strawberry"), testScheduler)
        val testSub = hotData.data.test()
        testSub.assertNotComplete()
        hotData.close()
        testScheduler.triggerActions()
        testSub.assertNoErrors()
        testSub.assertComplete()
    }

    @Test
    fun `init blocking constructor`() {
        val initializer = PublishSubject.create<String>()
        val hotData = HotData("TestThread") { initializer.blockingFirst() }

        hotData.data
                .timeout(1, TimeUnit.SECONDS)
                .test()
                .await().assertError(TimeoutException::class.java)

        initializer.onNext("cake")

        hotData.data.test().awaitCount(2).assertValue("cake")
    }

    @Test
    fun `updating values`() {
        val testSched = TestScheduler()
        val hotData = HotData(
                initialValue = { "strawberry" },
                scheduler = testSched
        )
        testSched.triggerActions()
        hotData.snapshot shouldBe "strawberry"
        hotData.update {
            it shouldBe "strawberry"
            "apple"
        }
        testSched.triggerActions()
        hotData.snapshot shouldBe "apple"
    }

    @Test
    fun `rx updating`() {
        val testSched = TestScheduler()
        val hotData = HotData(
                initialValue = { "strawberry" },
                scheduler = testSched
        )
        testSched.triggerActions()
        hotData.snapshot shouldBe "strawberry"
        val testSub = hotData.updateRx {
            it shouldBe "strawberry"
            "apple"
        }.test()
        testSched.triggerActions()
        hotData.snapshot shouldBe "apple"
        testSub.assertComplete().assertValue(HotData.Update("strawberry", "apple"))
    }

    @Test
    fun `rx update failing`() {
        val testSched = TestScheduler()
        val hotData = HotData(
                initialValue = { "strawberry" },
                scheduler = testSched
        )
        testSched.triggerActions()
        hotData.snapshot shouldBe "strawberry"

        val error = IllegalArgumentException()
        val testSub = hotData.updateRx {
            it shouldBe "strawberry"
            throw error
        }.test()
        testSched.triggerActions()
        hotData.snapshot shouldBe "strawberry"
        testSub.assertError(error)
    }
}