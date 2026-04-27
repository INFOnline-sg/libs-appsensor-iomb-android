package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLGestureEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLGestureEventTest {

    @Test
    void constructor_overloading() {
        new IOLGestureEvent(IOLGestureEvent.IOLGestureEventType.Shake);
        new IOLGestureEvent(IOLGestureEvent.IOLGestureEventType.Shake, "category", "comment");
        IOLEvent event = new IOLGestureEvent(IOLGestureEvent.IOLGestureEventType.Shake, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("gesture");

        assertThat(event.getState()).isEqualTo("shake");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
