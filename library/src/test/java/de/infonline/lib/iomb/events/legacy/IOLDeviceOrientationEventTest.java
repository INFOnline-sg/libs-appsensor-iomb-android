package de.infonline.lib.iomb.events.legacy;

import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import de.infonline.lib.iomb.IOLDeviceOrientationEvent;
import de.infonline.lib.iomb.IOLEvent;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class IOLDeviceOrientationEventTest {

    @Test
    public void constructor_overloading() {
        new IOLDeviceOrientationEvent(IOLDeviceOrientationEvent.IOLDeviceOrientationEventType.Changed);
        new IOLDeviceOrientationEvent(IOLDeviceOrientationEvent.IOLDeviceOrientationEventType.Changed, "category", "comment");
        IOLEvent event = new IOLDeviceOrientationEvent(IOLDeviceOrientationEvent.IOLDeviceOrientationEventType.Changed, "category", "comment", singletonMap("key", "value"));

        assertThat(event.getIdentifier()).isEqualTo("deviceOrientation");

        assertThat(event.getState()).isEqualTo("changed");
        assertThat(event.getCategory()).isEqualTo("category");
        assertThat(event.getComment()).isEqualTo("comment");
        assertThat(event.getCustomParams()).isEqualTo(singletonMap("key", "value"));
    }

    @Test
    public void build_result_includes_orientation() {
        IOLEvent event = new IOLDeviceOrientationEvent(IOLDeviceOrientationEvent.IOLDeviceOrientationEventType.Changed);
        Map<String, Object> objectMap = event.buildParameters(ApplicationProvider.getApplicationContext());
        assertThat(objectMap.get("orientation")).isEqualTo("1");
    }
}
