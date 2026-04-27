package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLIAPEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLIAPEventTest {


    @Test
    void constructor_overloading() {
        new IOLIAPEvent(IOLIAPEvent.IOLIAPEventType.Started);
        new IOLIAPEvent(IOLIAPEvent.IOLIAPEventType.Started, "category", "comment");
        IOLEvent event = new IOLIAPEvent(IOLIAPEvent.IOLIAPEventType.Started, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("iap");

        assertThat(event.getState()).isEqualTo("started");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
