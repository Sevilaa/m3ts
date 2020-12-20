package ch.m3ts.pubnub;

import android.content.Context;

import com.pubnub.api.Pubnub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PubNub Factory, use the static methods in here to instantiate the TrackerPubNub or DisplayPubNub
 * from an activity or fragment.
 */
public class PubNubFactory {
    private static final String PROP_FILE_NAME = "app.properties";

    private PubNubFactory() {}

    public static TrackerPubNub createTrackerPubNub(Context context, String roomId) {
        Properties properties = findProperties(context);
        Pubnub pubnub = createPubNub(properties);
        return new TrackerPubNub(pubnub, roomId);
    }

    public static DisplayPubNub createDisplayPubNub(Context context, String roomId) {
        Properties properties = findProperties(context);
        Pubnub pubnub = createPubNub(properties);
        return new DisplayPubNub(pubnub, roomId);
    }

    private static Pubnub createPubNub(Properties properties) {
        String pub = properties.getProperty("pub_key");
        String sub = properties.getProperty("sub_key");
        return new Pubnub(pub, sub);
    }


    private static Properties findProperties(Context context) {
        Properties properties = new Properties();
        try (InputStream is = context.getAssets().open(PROP_FILE_NAME)) {
            properties.load(is);
        } catch (IOException ex) {
            throw new NoPropertiesFileFoundException();
        }
        return properties;
    }

    public static class NoPropertiesFileFoundException extends RuntimeException {
        private static final String MESSAGE = "No "+PROP_FILE_NAME+ " file has been found in the assets directory!";
        NoPropertiesFileFoundException() {
            super(MESSAGE);
        }
    }
}
