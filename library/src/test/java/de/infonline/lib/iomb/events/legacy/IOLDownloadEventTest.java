package de.infonline.lib.iomb.events.legacy;

import org.junit.jupiter.api.Test;

import de.infonline.lib.iomb.IOLDownloadEvent;
import de.infonline.lib.iomb.IOLEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class IOLDownloadEventTest {


    @Test
    void constructor_overloading() {
        new IOLDownloadEvent(IOLDownloadEvent.IOLDownloadEventType.Succeeded);
        new IOLDownloadEvent(IOLDownloadEvent.IOLDownloadEventType.Succeeded, "category", "comment");
        IOLEvent event = new IOLDownloadEvent(IOLDownloadEvent.IOLDownloadEventType.Succeeded, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("download");

        assertThat(event.getState()).isEqualTo("succeeded");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }
}
