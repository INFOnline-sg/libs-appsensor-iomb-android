package testhelper;

import android.util.Base64;
import android.util.Base64InputStream;

import androidx.annotation.CallSuper;

import com.google.android.gms.common.util.IOUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.robolectric.shadows.ShadowLog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.util.zip.GZIPInputStream;

import kotlin.text.Charsets;
import timber.log.Timber;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaRobolectricBaseTest {

    @Before
    @CallSuper
    public void setup() throws Exception {
        ShadowLog.setupLogging();
        ShadowLog.stream = System.out;
        Timber.uprootAll();
        Timber.plant(new JUnitTree());
    }

    protected static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }


    protected static void resetSingleton(Class clazz, String fieldName) {
        Field instance;
        try {
            instance = clazz.getDeclaredField(fieldName);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static JSONObject requestToJSON(String requestBody) throws Exception {
        assertThat(requestBody).isNotNull();
        assertThat(requestBody).isNotEmpty();

        if (requestBody.startsWith("CCODE=")) {
            requestBody = requestBody.substring(9);
        }

        assertThat(requestBody).startsWith("ae=");
        String splitBody = requestBody.substring(3);
        assertThat(splitBody).isNotEmpty();
        String urlDecoded = URLDecoder.decode(splitBody, "UTF-8");

        InputStream bin = new ByteArrayInputStream(urlDecoded.getBytes(Charsets.UTF_8));
        GZIPInputStream gin = new GZIPInputStream(new Base64InputStream(bin, Base64.DEFAULT | Base64.NO_WRAP));

        String logEventContent = new String(IOUtils.toByteArray(gin));

        bin.close();
        gin.close();
        assertThat(logEventContent).isNotEmpty();

        JSONObject logEventJson = new JSONObject(logEventContent);
        return logEventJson;
    }

    protected static JSONObject zeroOutDynamicSendProps(JSONObject logEventJson) throws JSONException {
        JSONArray events = logEventJson.getJSONArray("events");
        for (int i = 0; i < events.length(); i++) {
            JSONObject oneEvent = logEventJson.getJSONArray("events").getJSONObject(i);
            assertThat(oneEvent.getDouble("timestamp")).isNotNull();
            oneEvent.put("timestamp", 0.0);
        }

        JSONObject clientJson = logEventJson.getJSONObject("client");
        JSONObject uuidJson = clientJson.getJSONObject("uuids");

        if (uuidJson.has("installationId")) {
            assertThat(uuidJson.getString("installationId")).isNotEmpty();
            uuidJson.put("installationId", "placeholder");
        }
        if (uuidJson.has("installationIdSHA256")) {
            assertThat(uuidJson.getString("installationIdSHA256")).isNotEmpty();
            uuidJson.put("installationIdSHA256", "placeholder");
        }

        JSONObject statsJson = logEventJson.getJSONObject("stats");
        assertThat(statsJson.getDouble("IOLConfigTTL")).isNotNull();
        statsJson.put("IOLConfigTTL", 0L);

        return logEventJson;
    }
}
