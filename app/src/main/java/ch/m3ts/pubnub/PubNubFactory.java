package ch.m3ts.pubnub;

import android.content.Context;

import com.pubnub.api.Pubnub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PubNubFactory {
    private static final String PROP_FILE_NAME = "app.properties";

    private PubNubFactory() {}

    public static TrackerPubNub createTrackerPubNub(Context context, String roomId) throws NoPropertiesFileFound {
        Properties properties = findProperties(context);
        String pub = properties.getProperty("pub_key");
        String sub = properties.getProperty("sub_key");
        Pubnub pubnub = new Pubnub(pub, sub);
        return new TrackerPubNub(pubnub, roomId);
    }


    private static Properties findProperties(Context context) {
        Properties properties = new Properties();
        try (InputStream is = context.getAssets().open(PROP_FILE_NAME)) {
            properties.load(is);
        } catch (IOException ex) {
            throw new NoPropertiesFileFound();
        }
        return properties;
    }

    public static class NoPropertiesFileFound extends RuntimeException {
        private static final String MESSAGE = "No "+PROP_FILE_NAME+ " file has been found in the assets directory!";
        NoPropertiesFileFound() {
            super(MESSAGE);
        }
    }
}
