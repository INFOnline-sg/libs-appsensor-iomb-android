package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLViewEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLViewEventTest {


    @Test
    void constructor_overloading() {
        new IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared);
        new IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared, "category", "comment");
        IOLEvent event = new IOLViewEvent(IOLViewEvent.IOLViewEventType.Appeared, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("view");

        assertThat(event.getState()).isEqualTo("appeared");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
