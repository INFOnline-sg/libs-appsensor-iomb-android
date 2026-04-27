package de.infonline.lib.iomb.events.internal

import de.infonline.lib.iomb.IOLEvent
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class IOLWebViewEventPrivateTest {

    @Test
    fun constructor_overloading() {
        IOLWebViewEventPrivate(IOLWebViewEventPrivate.IOLWebViewEventPrivateType.Init)
        IOLWebViewEventPrivate(IOLWebViewEventPrivate.IOLWebViewEventPrivateType.Init, "category", "comment")
        val event: IOLEvent = IOLWebViewEventPrivate(IOLWebViewEventPrivate.IOLWebViewEventPrivateType.Init, "category", "comment", Collections.singletonMap("key", "value"))

        Assertions.assertThat(event.identifier).isEqualTo("webView")

        Assertions.assertThat(event.state).isEqualTo("init")
        Assertions.assertThat(event.category).isEqualTo("category")
        Assertions.assertThat(event.comment).isEqualTo("comment")
        Assertions.assertThat(event.customParams).isEqualTo(Collections.singletonMap("key", "value"))
    }
}