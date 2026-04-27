package de.infonline.lib.iomb.util.rx

import java.util.*
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

internal class NamedThreadFactory(private val threadPrefix: String) : ThreadFactory {
    private val threadIndex = AtomicLong(1)

    override fun newThread(runnable: Runnable): Thread? = Thread(runnable).apply {
        name = if (threadPrefix.contains("%d")) {
            String.format(Locale.ROOT, threadPrefix, threadIndex.getAndIncrement())
        } else {
            "$threadPrefix-${threadIndex.getAndIncrement()}"
        }
    }
}