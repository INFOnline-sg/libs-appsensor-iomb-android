package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLAudioEvent;
import de.infonline.lib.iomb.IOLEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLAudioEventTest {

    @Test
    void constructor_overloading() {
        new IOLAudioEvent(IOLAudioEvent.IOLAudioEventType.Play);
        new IOLAudioEvent(IOLAudioEvent.IOLAudioEventType.Play, "category", "comment");
        IOLEvent event = new IOLAudioEvent(IOLAudioEvent.IOLAudioEventType.Play, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("audio");

        assertThat(event.getState()).isEqualTo("play");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
