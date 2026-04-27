package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLPushEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLPushEventTest {


    @Test
    void constructor_overloading() {
        new IOLPushEvent(IOLPushEvent.IOLPushEventType.Received);
        new IOLPushEvent(IOLPushEvent.IOLPushEventType.Received, "category", "comment");
        IOLEvent event = new IOLPushEvent(IOLPushEvent.IOLPushEventType.Received, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("push");

        assertThat(event.getState()).isEqualTo("received");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
