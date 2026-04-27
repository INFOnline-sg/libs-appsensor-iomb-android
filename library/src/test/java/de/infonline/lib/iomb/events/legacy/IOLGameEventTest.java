package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLGameEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLGameEventTest {


    @Test
    void constructor_overloading() {
        new IOLGameEvent(IOLGameEvent.IOLGameEventType.Won);
        new IOLGameEvent(IOLGameEvent.IOLGameEventType.Won, "category", "comment");
        IOLEvent event = new IOLGameEvent(IOLGameEvent.IOLGameEventType.Won, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("game");

        assertThat(event.getState()).isEqualTo("won");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
