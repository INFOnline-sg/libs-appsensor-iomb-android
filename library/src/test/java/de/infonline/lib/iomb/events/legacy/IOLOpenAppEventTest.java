package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLOpenAppEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLOpenAppEventTest {

    @Test
    void constructor_overloading() {
        new IOLOpenAppEvent(IOLOpenAppEvent.IOLOpenAppEventType.Maps);
        new IOLOpenAppEvent(IOLOpenAppEvent.IOLOpenAppEventType.Maps, "category", "comment");
        IOLEvent event = new IOLOpenAppEvent(IOLOpenAppEvent.IOLOpenAppEventType.Maps, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("openApp");

        assertThat(event.getState()).isEqualTo("maps");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
