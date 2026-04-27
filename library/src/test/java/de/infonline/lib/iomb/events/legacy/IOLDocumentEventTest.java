package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLDocumentEvent;
import de.infonline.lib.iomb.IOLEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLDocumentEventTest {


    @Test
    void constructor_overloading() {
        new IOLDocumentEvent(IOLDocumentEvent.IOLDocumentEventType.Open);
        new IOLDocumentEvent(IOLDocumentEvent.IOLDocumentEventType.Open, "category", "comment");
        IOLEvent event = new IOLDocumentEvent(IOLDocumentEvent.IOLDocumentEventType.Open, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("document");

        assertThat(event.getState()).isEqualTo("open");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
