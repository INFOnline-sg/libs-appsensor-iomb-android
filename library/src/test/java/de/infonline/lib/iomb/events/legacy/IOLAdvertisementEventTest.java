package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLAdvertisementEvent;
import de.infonline.lib.iomb.IOLEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLAdvertisementEventTest {


    @Test
    void constructor_overloading() {
        new IOLAdvertisementEvent(IOLAdvertisementEvent.IOLAdvertisementEventType.Open);
        new IOLAdvertisementEvent(IOLAdvertisementEvent.IOLAdvertisementEventType.Open, "category", "comment");
        IOLEvent event = new IOLAdvertisementEvent(IOLAdvertisementEvent.IOLAdvertisementEventType.Open, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("advertisement");

        assertThat(event.getState()).isEqualTo("open");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
