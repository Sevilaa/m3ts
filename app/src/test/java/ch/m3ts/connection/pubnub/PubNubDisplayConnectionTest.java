package ch.m3ts.connection.pubnub;

import android.graphics.Point;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Random;

import ch.m3ts.display.DisplayConnectCallback;
import ch.m3ts.display.GameListener;
import ch.m3ts.event.Event;
import ch.m3ts.event.Subscribable;
import ch.m3ts.event.TTEventBus;
import ch.m3ts.event.data.StatusUpdateData;
import ch.m3ts.event.data.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import ch.m3ts.tabletennis.match.MatchType;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

class StubListenerDisplay implements Subscribable {
    private final GameListener gameListener;
    private final DisplayUpdateListener displayUpdateListener;

    StubListenerDisplay(DisplayUpdateListener displayUpdateListener, GameListener gameListener) {
        this.displayUpdateListener = displayUpdateListener;
        this.gameListener = gameListener;
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof ToDisplayData) {
            ToDisplayData toDisplayData = (ToDisplayData) data;
            toDisplayData.call(displayUpdateListener);
        } else if (data instanceof StatusUpdateData) {
            StatusUpdateData updateData = (StatusUpdateData) data;
            gameListener.onStatusUpdate(updateData.getPlayerNameLeft(), updateData.getPlayerNameRight(),
                    updateData.getPointsLeft(), updateData.getPointsRight(), updateData.getGamesLeft(),
                    updateData.getGamesRight(), updateData.getNextServer(), updateData.getGamesNeededToWin());
        }
    }
}

@RunWith(PowerMockRunner.class)
@PrepareForTest(Pubnub.class)
public class PubNubDisplayConnectionTest {
    private Random random = new Random();
    private Pubnub pubnub;
    private final static String ROOM_ID = "invalid";
    private DisplayUpdateListener spyCallback;
    private GameListener deCallback;
    private StubListenerDisplay stubListener;

    @Before
    public void setup() {
        this.pubnub = spy(mock(Pubnub.class));
        this.spyCallback = spy(mock(DisplayUpdateListener.class));
        this.deCallback = spy(mock(GameListener.class));
        this.stubListener = new StubListenerDisplay(spyCallback, deCallback);
        TTEventBus.getInstance().register(stubListener);
    }

    @After
    public void tearDown() {
        this.pubnub = null;
        TTEventBus.getInstance().unregister(stubListener);
    }

    @Test
    public void testOnMatchEnded() throws JSONException {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        JSONObject jsonMatchEnded = makeJSONObject("onMatchEnded", Side.LEFT, null, null, null);
        pubNubDisplayConnection.connectCallback(ROOM_ID, jsonMatchEnded);
        verify(spyCallback, times(0)).onMatchEnded(Side.LEFT.toString());
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonMatchEnded);
        verify(spyCallback, times(1)).onMatchEnded(Side.LEFT.toString());
    }

    @Test
    public void testOnScore() throws JSONException {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        final Side scorer = Side.RIGHT;
        final Side server = Side.LEFT;
        final int score = random.nextInt(999);
        JSONObject jsonScore = makeJSONObject("onScore", scorer, score, null, server, server);
        pubNubDisplayConnection.connectCallback(ROOM_ID, jsonScore);
        verify(spyCallback, times(0)).onScore(Side.RIGHT, jsonScore.getInt("score"), Side.LEFT, server);
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonScore);
        verify(spyCallback, times(1)).onScore(Side.RIGHT, jsonScore.getInt("score"), Side.LEFT, server);
    }

    @Test
    public void testOnWin() throws JSONException {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        final Side winner = Side.LEFT;
        final Side server = Side.RIGHT;
        final int wins = random.nextInt(999);
        JSONObject jsonWin = makeJSONObject("onWin", winner, null, wins, server);
        pubNubDisplayConnection.connectCallback(ROOM_ID, jsonWin);
        verify(spyCallback, times(0)).onWin(Side.LEFT, jsonWin.getInt("wins"));
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonWin);
        verify(spyCallback, times(1)).onWin(Side.LEFT, jsonWin.getInt("wins"));
    }

    @Test
    public void testOnReadyToServe() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        final Side server = Side.RIGHT;
        JSONObject jsonReadyToServe = makeJSONObject("onReadyToServe", server, null, null, null);
        pubNubDisplayConnection.connectCallback(ROOM_ID, jsonReadyToServe);
        verify(spyCallback, times(0)).onReadyToServe(server);
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonReadyToServe);
        verify(spyCallback, times(1)).onReadyToServe(server);
    }

    @Test
    public void testOnStatusUpdate() throws JSONException {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        String pL = "leftDude";
        String pR = "rightDude";
        int sL = random.nextInt(999);
        int sR = random.nextInt(999);
        int wL = random.nextInt(999);
        int wR = random.nextInt(999);
        int gN = random.nextInt(999);
        Side nextServer = Side.RIGHT;
        JSONObject jsonStatus = new JSONObject();
        jsonStatus.put(JSONInfo.EVENT_PROPERTY, "onStatusUpdate");
        jsonStatus.put(JSONInfo.PLAYER_NAME_LEFT_PROPERTY, pL);
        jsonStatus.put(JSONInfo.PLAYER_NAME_RIGHT_PROPERTY, pR);
        jsonStatus.put(JSONInfo.SCORE_LEFT_PROPERTY, sL);
        jsonStatus.put(JSONInfo.SCORE_RIGHT_PROPERTY, sR);
        jsonStatus.put(JSONInfo.WINS_LEFT_PROPERTY, wL);
        jsonStatus.put(JSONInfo.WINS_RIGHT_PROPERTY, wR);
        jsonStatus.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
        jsonStatus.put(JSONInfo.GAMES_NEEDED_PROPERTY, gN);
        pubNubDisplayConnection.connectCallback(ROOM_ID, jsonStatus);
        verify(deCallback, times(0)).onStatusUpdate(pL, pR, sL, sR, wL, wR, nextServer, gN);
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonStatus);
        verify(deCallback, times(1)).onStatusUpdate(pL, pR, sL, sR, wL, wR, nextServer, gN);
    }

    @Test
    public void testOnConnected() throws JSONException {
        DisplayConnectCallback connectCallback = spy(mock(DisplayConnectCallback.class));
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        JSONObject jsonStatus = new JSONObject();
        jsonStatus.put(JSONInfo.EVENT_PROPERTY, "onConnected");
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonStatus);
        verify(connectCallback, times(0)).onConnected();
        pubNubDisplayConnection.setDisplayConnectCallback(connectCallback);
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonStatus);
        verify(connectCallback, times(1)).onConnected();
    }

    @Test
    public void testOnPauseJSON() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        assertJSONWithEvent("onPause");
        pubNubDisplayConnection.onPause();
    }

    @Test
    public void testOnResumeJSON() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        assertJSONWithEvent("onResume");
        pubNubDisplayConnection.onResume();
    }

    @Test
    public void testOnRequestTableFrame() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        assertJSONWithEvent("onRequestTableFrame");
        pubNubDisplayConnection.onRequestTableFrame();
    }

    @Test
    public void testOnPointAdditionJSON() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        final Side sideToBeAddedTo = Side.LEFT;
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                assertEquals("onPointAddition", json.getString(JSONInfo.EVENT_PROPERTY));
                assertEquals(sideToBeAddedTo, Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)));
                assertEquals(ROOM_ID, channelName);
                return null;
            }
        }).when(pubnub).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        pubNubDisplayConnection.onPointAddition(sideToBeAddedTo);
    }

    @Test
    public void testOnSelectTableCornersJSON() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        final Point[] tableCorners = new Point[6];
        tableCorners[0] = new Point(5, 60);
        tableCorners[1] = new Point(95, 61);
        tableCorners[2] = new Point(90, 53);
        tableCorners[3] = new Point(11, 54);
        tableCorners[4] = new Point(52, 60);
        tableCorners[5] = new Point(50, 53);
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                JSONArray intTableCornersArray = json.getJSONArray(JSONInfo.CORNERS);
                assertEquals(tableCorners.length*2, intTableCornersArray.length());
                for (int i = 0; i < tableCorners.length; i++) {
                    int j = i * 2;
                    assertEquals(tableCorners[i].x, intTableCornersArray.getInt(j));
                    assertEquals(tableCorners[i].y, intTableCornersArray.getInt(j + 1));
                }
                assertEquals("onSelectTableCorner", json.getString(JSONInfo.EVENT_PROPERTY));
                assertEquals(ROOM_ID, channelName);
                return null;
            }
        }).when(pubnub).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        pubNubDisplayConnection.onSelectTableCorners(tableCorners);
    }

    @Test
    public void testOnTableFrameSuccess() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        DisplayConnectCallback displayConnectCallback = spy(mock(DisplayConnectCallback.class));
        pubNubDisplayConnection.setDisplayConnectCallback(displayConnectCallback);
        final String baseImg = "I'm an encoded byte array";
        String baseImgP1 = "I'm an encoded ";
        String baseImgP2 = "byte array";
        final int h = 720;
        final int w = 1280;
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                byte[] baseBytes = (byte[]) invocation.getArguments()[0];
                int tableWidth = (int) invocation.getArguments()[1];
                int tableHeight = (int) invocation.getArguments()[2];
                assertEquals(w, tableWidth);
                assertEquals(h, tableHeight);
                String res = ByteToBase64.encodeToString(baseBytes);
                assertEquals(baseImg, res);
                return null;
            }
        }).when(displayConnectCallback).onImageReceived(any(byte[].class), anyInt(), anyInt());

        JSONObject jsonTableFrame1of2 = makeJSONObject("onTableFrame", 0, 2, baseImgP1, w, h);
        JSONObject jsonTableFrame2of2 = makeJSONObject("onTableFrame", 1, 2, baseImgP2, w, h);
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonTableFrame1of2);
        verify(displayConnectCallback, times(1)).onImageTransmissionStarted(2);
        verify(displayConnectCallback, times(0)).onImagePartReceived(anyInt());
        verify(displayConnectCallback, times(0)).onImageReceived(any(byte[].class), anyInt(), anyInt());
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonTableFrame2of2);
        verify(displayConnectCallback, times(1)).onImageTransmissionStarted(2);
        verify(displayConnectCallback, times(1)).onImagePartReceived(2);
        verify(displayConnectCallback, times(1)).onImageReceived(any(byte[].class), anyInt(), anyInt());

    }

    @Test
    public void testOnTableFrameFailure() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        DisplayConnectCallback displayConnectCallback = spy(mock(DisplayConnectCallback.class));
        pubNubDisplayConnection.setDisplayConnectCallback(displayConnectCallback);
        String baseImgP1 = "I'm an encoded ";
        String baseImgP2 = "byte";
        String baseImgP3 = " array";
        final int h = 720;
        final int w = 1280;
        JSONObject jsonTableFrame1of3 = makeJSONObject("onTableFrame", 0, 3, baseImgP1, w, h);
        JSONObject jsonTableFrame2of3 = makeJSONObject("onTableFrame", 1, 3, baseImgP2, w, h);
        JSONObject jsonTableFrame3of3 = makeJSONObject("onTableFrame", 2, 3, baseImgP2, w, h);

        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                assertEquals("onRequestTableFrame", json.getString(JSONInfo.EVENT_PROPERTY));
                assertEquals(ROOM_ID, channelName);
                return null;
            }
        }).when(pubnub).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        verify(pubnub, times(0)).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonTableFrame1of3);
        pubNubDisplayConnection.successCallback(ROOM_ID, jsonTableFrame3of3);
        // display now notices that a part is missing -> onRequestTableFrame() gets executed which publishes an event
        verify(pubnub, times(1)).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        verify(displayConnectCallback, times(0)).onImageReceived(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    public void testOnPointDeductionJSON() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        final Side sideToBeDeducted = Side.RIGHT;
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                assertEquals("onPointDeduction", json.getString(JSONInfo.EVENT_PROPERTY));
                assertEquals(sideToBeDeducted, Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)));
                assertEquals(ROOM_ID, channelName);
                return null;
            }
        }).when(pubnub).publish(any(String.class), any(JSONObject.class), any(Callback.class));
        pubNubDisplayConnection.onPointDeduction(sideToBeDeducted);
    }

    @Test
    public void testRequestStatusUpdateJSON() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        assertJSONWithEvent("requestStatus");
        pubNubDisplayConnection.requestStatusUpdate();
    }

    @Test
    public void testOnStartMatch() {
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        assertJSONWithEvent("onStartMatch");
        pubNubDisplayConnection.onStartMatch(MatchType.BO1.toString(), Side.LEFT.toString());
    }

    private void assertJSONWithEvent(final String event) {
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String channelName = (String) invocation.getArguments()[0];
                JSONObject json = (JSONObject) invocation.getArguments()[1];
                assertEquals(event, json.getString(JSONInfo.EVENT_PROPERTY));
                assertEquals(ROOM_ID, channelName);
                return null;
            }
        }).when(this.pubnub).publish(any(String.class), any(JSONObject.class), any(Callback.class));
    }

    @Test
    public void testWithInvalidJSON() {
        try {
            PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
            for (int i = 0; i < 100; i++) {
                JSONObject invalidJSON = makeJSONObject(generateRandomAlphabeticString(random.nextInt(20)),
                        Side.values()[random.nextInt(4)], random.nextInt(999), random.nextInt(999), Side.values()[random.nextInt(4)]);
                pubNubDisplayConnection.connectCallback(ROOM_ID, invalidJSON);
                pubNubDisplayConnection.disconnectCallback(ROOM_ID, invalidJSON);
                pubNubDisplayConnection.successCallback(ROOM_ID, invalidJSON);
                verify(spyCallback, times(0)).onScore(any(Side.class), anyInt(), any(Side.class), any(Side.class));
                verify(spyCallback, times(0)).onWin(any(Side.class), anyInt());
                verify(spyCallback, times(0)).onMatchEnded(any(String.class));
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testUnsubscribe() throws Exception {
        doNothing().when(pubnub).unsubscribe(anyString());
        PubNubDisplayConnection pubNubDisplayConnection = new PubNubDisplayConnection(pubnub, ROOM_ID);
        verify(pubnub, times(0)).unsubscribe(ROOM_ID);
        verify(pubnub, times(1)).subscribe(ROOM_ID, pubNubDisplayConnection);
        pubNubDisplayConnection.unsubscribe();
        verify(pubnub, times(1)).unsubscribe(ROOM_ID);
        verify(pubnub, times(1)).subscribe(ROOM_ID, pubNubDisplayConnection);
    }


    private JSONObject makeJSONObject(String event, Side side, Integer score, Integer wins, Side nextServer) {
        JSONObject json = new JSONObject();
        try {
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.WINS_PROPERTY, wins);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
        } catch (JSONException ex) {
            fail(ex.getMessage());
        }
        return json;
    }

    private JSONObject makeJSONObject(String event, Side side, Integer score, Integer wins, Side nextServer, Side lastServer) {
        JSONObject json = new JSONObject();
        try {
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.WINS_PROPERTY, wins);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            json.put(JSONInfo.LAST_SERVER_PROPERTY, lastServer);
        } catch (JSONException ex) {
            fail(ex.getMessage());
        }
        return json;
    }

    private JSONObject makeJSONObject(String event, int tableFrameIndex, int tableFrameNoOfParts, String base64EncodedPart, int width, int height) {
        JSONObject json = new JSONObject();
        try {
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.TABLE_FRAME_INDEX, tableFrameIndex);
            json.put(JSONInfo.TABLE_FRAME_NUMBER_OF_PARTS, tableFrameNoOfParts);
            json.put(JSONInfo.TABLE_FRAME, base64EncodedPart);
            json.put(JSONInfo.TABLE_FRAME_HEIGHT, height);
            json.put(JSONInfo.TABLE_FRAME_WIDTH, width);
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