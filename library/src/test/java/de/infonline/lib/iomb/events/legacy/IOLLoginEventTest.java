package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLLoginEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLLoginEventTest {

    @Test
    void constructor_overloading() {
        new IOLLoginEvent(IOLLoginEvent.IOLLoginEventType.Succeeded);
        new IOLLoginEvent(IOLLoginEvent.IOLLoginEventType.Succeeded, "category", "comment");
        IOLEvent event = new IOLLoginEvent(IOLLoginEvent.IOLLoginEventType.Succeeded, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("login");

        assertThat(event.getState()).isEqualTo("succeeded");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
