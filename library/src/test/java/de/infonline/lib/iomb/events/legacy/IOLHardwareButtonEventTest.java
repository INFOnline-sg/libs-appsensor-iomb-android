package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLHardwareButtonEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLHardwareButtonEventTest {

    @Test
    void constructor_overloading() {
        new IOLHardwareButtonEvent(IOLHardwareButtonEvent.IOLHardwareButtonEventType.Pushed);
        new IOLHardwareButtonEvent(IOLHardwareButtonEvent.IOLHardwareButtonEventType.Pushed, "category", "comment");
        IOLEvent event = new IOLHardwareButtonEvent(IOLHardwareButtonEvent.IOLHardwareButtonEventType.Pushed, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("hardwareButton");

        assertThat(event.getState()).isEqualTo("pushed");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
