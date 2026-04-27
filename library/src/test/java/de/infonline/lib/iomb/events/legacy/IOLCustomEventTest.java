package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLCustomEvent;
import de.infonline.lib.iomb.IOLEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLCustomEventTest {

    @Test
    void constructor_overloading() {
        new IOLCustomEvent("state");
        new IOLCustomEvent("state", "category", "comment");
        IOLEvent event = new IOLCustomEvent("state", "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("custom");

        assertThat(event.getState()).isEqualTo("state");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
