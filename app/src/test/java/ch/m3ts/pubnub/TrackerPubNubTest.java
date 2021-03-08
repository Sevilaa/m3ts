package ch.m3ts.pubnub;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Random;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchStatus;
import ch.m3ts.tabletennis.match.MatchStatusCallback;
import ch.m3ts.tabletennis.match.game.ScoreManipulationCallback;
import ch.m3ts.tracker.init.InitTrackerCallback;
import ch.m3ts.tracker.visualization.MatchVisualizeHandlerCallback;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Pubnub.class)
public class TrackerPubNubTest {
    private TrackerPubNub trackerPubNub;
    private Pubnub pubNub;

    @Before
    public void init() {
        pubNub = PowerMockito.spy(PowerMockito.mock(Pubnub.class));
        trackerPubNub = new TrackerPubNub(pubNub, "invalid");
    }

    @After
    public void tearDown() {
        validateMockitoUsage();
    }

    @Test
    public void testReceivingPointAdditionAndDeduction() {
        try {
            ScoreManipulationCallback spyCallback = PowerMockito.spy(PowerMockito.mock(ScoreManipulationCallback.class));
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

    @Test
    public void testPausingAndResumingMatch() throws Exception {
        ScoreManipulationCallback spyCallback = PowerMockito.spy(PowerMockito.mock(ScoreManipulationCallback.class));
        trackerPubNub.setScoreManipulationCallback(spyCallback);
        verify(spyCallback, times(0)).onPause();
        verify(spyCallback, times(0)).onResume();
        JSONObject jsonObject = makeJSONObject("onPause");
        trackerPubNub.successCallback("test", jsonObject);
        verify(spyCallback, times(1)).onPause();
        jsonObject = makeJSONObject("onResume");
        trackerPubNub.successCallback("test", jsonObject);
        verify(spyCallback, times(1)).onResume();
    }

    @Test
    public void testRestartingMatch() throws Exception {
        MatchVisualizeHandlerCallback spyCallback = PowerMockito.spy(PowerMockito.mock(MatchVisualizeHandlerCallback.class));
        JSONObject jsonObject = makeJSONObject("onRestartMatch");
        // check for exception
        trackerPubNub.successCallback("test", jsonObject);
        verify(spyCallback, times(0)).restartMatch();
        trackerPubNub.setMatchVisualizeHandlerCallback(spyCallback);
        trackerPubNub.successCallback("test", jsonObject);
        verify(spyCallback, times(1)).restartMatch();
    }

    @Test
    public void testUsesInitTracker() throws Exception {
        int h = 100;
        int w = 200;
        byte[] randomBytes = new byte[w*h];
        new Random().nextBytes(randomBytes);
        InitTrackerCallback spyCallback = PowerMockito.spy(PowerMockito.mock(InitTrackerCallback.class));
        when(spyCallback.getCameraHeight()).thenReturn(h);
        when(spyCallback.getCameraWidth()).thenReturn(w);
        when(spyCallback.onCaptureFrame()).thenReturn(randomBytes);
        trackerPubNub.setInitTrackerCallback(spyCallback);

        // test the workflow
        // firstly a table frame is requested
        verify(spyCallback, times(0)).getCameraHeight();
        verify(spyCallback, times(0)).getCameraWidth();
        verify(spyCallback, times(0)).onCaptureFrame();
        verify(spyCallback, times(0)).setLoadingBarSize(anyInt());
        JSONObject jsonObject = makeJSONObject("onRequestTableFrame");
        trackerPubNub.successCallback("test", jsonObject);
        verify(spyCallback, times(2)).getCameraHeight();
        verify(spyCallback, times(2)).getCameraWidth();
        verify(spyCallback, times(1)).onCaptureFrame();
        verify(spyCallback, times(1)).setLoadingBarSize(1);

        // secondly the display will select some corners
        final int[] tableCorners = new int[8];
        jsonObject = makeJSONObject("onSelectTableCorner", tableCorners);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                int[] tableCornersCalled =  (int[]) invocation.getArguments()[0];
                for(int i =0; i<tableCornersCalled.length; i++) {
                    assertEquals(tableCorners[i], tableCornersCalled[i]);
                }
                return null;
            }
        }).when(spyCallback).setTableCorners(any(int[].class));
        verify(spyCallback, times(0)).setTableCorners(any(int[].class));
        trackerPubNub.successCallback("test", jsonObject);
        verify(spyCallback, times(1)).setTableCorners(any(int[].class));


        // last, the display will tell "start the match"
        jsonObject = makeJSONObject("onStartMatch");
        verify(spyCallback, times(0)).switchToLiveActivity();
        trackerPubNub.successCallback("test", jsonObject);
        verify(spyCallback, times(1)).switchToLiveActivity();
    }

    @Test
    public void testRequestMatchStatus() {
        final MatchStatus matchStatus = new MatchStatus("pl","pr",1,0,0,1, Side.RIGHT);
        MatchStatusCallback spyCallback = PowerMockito.spy(PowerMockito.mock(MatchStatusCallback.class));
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                assertEquals("invalid", channelName);
                assertEquals(matchStatus.getPlayerLeft(), json.getString(JSONInfo.PLAYER_NAME_LEFT_PROPERTY));
                assertEquals(matchStatus.getPlayerRight(), json.getString(JSONInfo.PLAYER_NAME_RIGHT_PROPERTY));
                assertEquals(matchStatus.getScoreLeft(), json.getInt(JSONInfo.SCORE_LEFT_PROPERTY));
                assertEquals(matchStatus.getScoreRight(), json.getInt(JSONInfo.SCORE_RIGHT_PROPERTY));
                assertEquals(matchStatus.getWinsLeft(), json.getInt(JSONInfo.WINS_LEFT_PROPERTY));
                assertEquals(matchStatus.getWinsRight(), json.getInt(JSONInfo.WINS_RIGHT_PROPERTY));
                assertEquals(matchStatus.getNextServer(), Side.valueOf(json.getString(JSONInfo.NEXT_SERVER_PROPERTY)));
                return null;
            }
        }).when(pubNub).publish(any(String.class), any(JSONObject.class), any(Callback.class));

        when(spyCallback.onRequestMatchStatus()).thenReturn(matchStatus);
        JSONObject json = makeJSONObject("requestStatus");
        trackerPubNub.successCallback("test", json);
        verify(spyCallback, times(0)).onRequestMatchStatus();
        trackerPubNub.setTrackerPubNubCallback(spyCallback);
        trackerPubNub.successCallback("test", json);
    }

    @Test
    public void testOnScore() {
        final int score = 7;
        final Side scorer = Side.LEFT;
        final Side server = Side.RIGHT;
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                assertEquals(score, json.getInt(JSONInfo.SCORE_PROPERTY));
                assertEquals(server, Side.valueOf(json.getString(JSONInfo.NEXT_SERVER_PROPERTY)));
                assertEquals(scorer, Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)));
                assertEquals("onScore", json.getString(JSONInfo.EVENT_PROPERTY));
                assertEquals("invalid", channelName);
                return null;
            }
        }).when(pubNub).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        trackerPubNub.onScore(scorer, score, server, server);
    }

    @Test
    public void testOnWin() {
        final int wins = 2;
        final Side winner = Side.RIGHT;
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                assertEquals(wins, json.getInt(JSONInfo.SCORE_PROPERTY));
                assertEquals(winner, Side.valueOf(json.getString(JSONInfo.NEXT_SERVER_PROPERTY)));
                assertEquals("onWin", json.getString(JSONInfo.EVENT_PROPERTY));
                assertEquals("invalid", channelName);
                return null;
            }
        }).when(pubNub).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        trackerPubNub.onWin(winner, wins);
    }

    @Test
    public void testOnMatchEnded() {
        final String winnerName = "Klaus";
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                assertEquals(winnerName, json.getString(JSONInfo.SIDE_PROPERTY));
                assertEquals("onMatchEnded", json.getString(JSONInfo.EVENT_PROPERTY));
                assertEquals("invalid", channelName);
                return null;
            }
        }).when(pubNub).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        trackerPubNub.onMatchEnded(winnerName);
    }

    @Test
    public void testOnReadyToServe() {
        final Side server = Side.LEFT;
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                assertEquals(server, Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)));
                assertEquals("onReadyToServe", json.getString(JSONInfo.EVENT_PROPERTY));
                assertEquals("invalid", channelName);
                return null;
            }
        }).when(pubNub).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        trackerPubNub.onReadyToServe(server);
    }

    private JSONObject makeJSONObject(String event) {
        JSONObject json = new JSONObject();
        try {
            json.put("event", event);
        } catch (JSONException ex) {
            fail(ex.getMessage());
        }
        return json;
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

    private JSONObject makeJSONObject(String event, int[] array) {
        JSONObject json = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray(array);
            json.put(JSONInfo.CORNERS, jsonArray);
            json.put("event", event);
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