package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLVideoEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLVideoEventTest {


    @Test
    void constructor_overloading() {
        new IOLVideoEvent(IOLVideoEvent.IOLVideoEventType.Play);
        new IOLVideoEvent(IOLVideoEvent.IOLVideoEventType.Play, "category", "comment");
        IOLEvent event = new IOLVideoEvent(IOLVideoEvent.IOLVideoEventType.Play, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("video");

        assertThat(event.getState()).isEqualTo("play");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
