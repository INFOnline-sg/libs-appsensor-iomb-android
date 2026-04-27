package de.infonline.lib.iomb.events.internal

import de.infonline.lib.iomb.IOLEvent
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class IOLInternetConnectionEventPrivateTest {

    @Test
    fun constructor_overloading() {
        IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivate.IOLInternetConnectionEventPrivateType.Established)
        IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivate.IOLInternetConnectionEventPrivateType.Established, "category", "comment")
        val event: IOLEvent = IOLInternetConnectionEventPrivate(IOLInternetConnectionEventPrivate.IOLInternetConnectionEventPrivateType.Established, "category", "comment", Collections.singletonMap("key", "value"))
        Assertions.assertThat(event.identifier).isEqualTo("internetConnection")
        Assertions.assertThat(event.state).isEqualTo("established")
        Assertions.assertThat(event.category).isEqualTo("category")
        Assertions.assertThat(event.comment).isEqualTo("comment")
        Assertions.assertThat(event.customParams).isEqualTo(Collections.singletonMap("key", "value"))
    }
}