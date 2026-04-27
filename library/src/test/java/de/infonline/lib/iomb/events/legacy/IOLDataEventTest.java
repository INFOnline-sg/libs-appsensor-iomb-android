package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLDataEvent;
import de.infonline.lib.iomb.IOLEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLDataEventTest {

    @Test
    void constructor_overloading() {
        new IOLDataEvent(IOLDataEvent.IOLDataEventType.Refresh);
        new IOLDataEvent(IOLDataEvent.IOLDataEventType.Refresh, "category", "comment");
        IOLEvent event = new IOLDataEvent(IOLDataEvent.IOLDataEventType.Refresh, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("data");

        assertThat(event.getState()).isEqualTo("refresh");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
