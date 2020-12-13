package cz.fmo;

import com.pubnub.api.Pubnub;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Random;

import ch.m3ts.pubnub.TrackerPubNub;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.game.ScoreManipulationCallback;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Pubnub.class)
public class TrackerPubNubTest {

    @Test
    public void testReceivingPointAdditionAndDeduction() {
        try {
            ScoreManipulationCallback spyCallback = spy(mock(ScoreManipulationCallback.class));
            Pubnub pubnubSpy = spy(mock(Pubnub.class));
            PowerMockito.whenNew(Pubnub.class).withArguments("invalid", "invalid").thenReturn(pubnubSpy);
            TrackerPubNub trackerPubNub = new TrackerPubNub("test", "invalid", "invalid");
            trackerPubNub.setScoreManipulationCallback(spyCallback);

            // test onPointAddition
            JSONObject jsonObject = makeJSONObject("onPointAddition", Side.LEFT);
            trackerPubNub.successCallback("test", jsonObject);
            verify(spyCallback, times(1)).onPointAddition(Side.LEFT);
            jsonObject = makeJSONObject("onPointAddition", Side.RIGHT);
            trackerPubNub.successCallback("test", jsonObject);
            verify(spyCallback, times(1)).onPointAddition(Side.RIGHT);

            // test onPointDeduction
            jsonObject = makeJSONObject("onPointDeduction", Side.LEFT);
            trackerPubNub.successCallback("test", jsonObject);
            verify(spyCallback, times(1)).onPointDeduction(Side.LEFT);
            jsonObject = makeJSONObject("onPointDeduction", Side.RIGHT);
            trackerPubNub.successCallback("test", jsonObject);
            verify(spyCallback, times(1)).onPointDeduction(Side.RIGHT);

            // test with Random events
            Random r = new Random();
            for (int i = 0; i<100; i++) {
                jsonObject = makeJSONObject(generateRandomAlphabeticString(r.nextInt(20)), Side.LEFT);
                trackerPubNub.successCallback("test", jsonObject);
                jsonObject = makeJSONObject(generateRandomAlphabeticString(r.nextInt(20)), Side.RIGHT);
                trackerPubNub.successCallback("test", jsonObject);
                verify(spyCallback, times(1)).onPointDeduction(Side.LEFT);
                verify(spyCallback, times(1)).onPointAddition(Side.RIGHT);
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    private JSONObject makeJSONObject(String event, Side side) {
        JSONObject json = new JSONObject();
        try {
            json.put("event", event);
            json.put("side", side);
        } catch (JSONException ex) {
            fail(ex.getMessage());
        }
        return json;
    }

    private String generateRandomAlphabeticString(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
}