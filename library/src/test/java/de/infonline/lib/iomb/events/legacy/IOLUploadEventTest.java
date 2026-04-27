package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLEvent;
import de.infonline.lib.iomb.IOLUploadEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLUploadEventTest {


    @Test
    void constructor_overloading() {
        new IOLUploadEvent(IOLUploadEvent.IOLUploadEventType.Cancelled);
        new IOLUploadEvent(IOLUploadEvent.IOLUploadEventType.Cancelled, "category", "comment");
        IOLEvent event = new IOLUploadEvent(IOLUploadEvent.IOLUploadEventType.Cancelled, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("upload");

        assertThat(event.getState()).isEqualTo("cancelled");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
