package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLBackgroundTaskEvent;
import de.infonline.lib.iomb.IOLEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLBackgroundTaskEventTest {


    @Test
    void constructor_overloading() {
        new IOLBackgroundTaskEvent(IOLBackgroundTaskEvent.IOLBackgroundTaskEventType.Start);
        new IOLBackgroundTaskEvent(IOLBackgroundTaskEvent.IOLBackgroundTaskEventType.Start, "category", "comment");
        IOLEvent event = new IOLBackgroundTaskEvent(IOLBackgroundTaskEvent.IOLBackgroundTaskEventType.Start, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("backgroundTask");

        assertThat(event.getState()).isEqualTo("start");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
