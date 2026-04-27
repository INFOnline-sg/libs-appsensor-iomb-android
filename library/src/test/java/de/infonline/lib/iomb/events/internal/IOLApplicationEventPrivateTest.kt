package de.infonline.lib.iomb.events.internal

import de.infonline.lib.iomb.IOLEvent
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class IOLApplicationEventPrivateTest {


    @Test
    fun constructor_overloading() {
        IOLApplicationEventPrivate(IOLApplicationEventPrivate.IOLApplicationEventPrivateType.Start)
        IOLApplicationEventPrivate(IOLApplicationEventPrivate.IOLApplicationEventPrivateType.Start, "category", "comment")
        val event: IOLEvent = IOLApplicationEventPrivate(IOLApplicationEventPrivate.IOLApplicationEventPrivateType.Start, "category", "comment", Collections.singletonMap("key", "value"))
        Assertions.assertThat(event.identifier).isEqualTo("application")
        Assertions.assertThat(event.state).isEqualTo("start")
        Assertions.assertThat(event.category).isEqualTo("category")
        Assertions.assertThat(event.comment).isEqualTo("comment")
        Assertions.assertThat(event.customParams).isEqualTo(Collections.singletonMap("key", "value"))
    }
}