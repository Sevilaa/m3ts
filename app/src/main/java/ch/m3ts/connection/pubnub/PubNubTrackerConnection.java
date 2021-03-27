package ch.m3ts.connection.pubnub;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import ch.m3ts.Log;
import ch.m3ts.connection.ImplTrackerConnection;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.UICallback;

public class PubNubTrackerConnection extends ImplTrackerConnection implements UICallback {
    private static final String JSON_SEND_EXCEPTION_MESSAGE = "Unable to send JSON to channel ";
    private static final String ROLE = "tracker";
    private final Pubnub pubnub;
    private final String roomID;

    public PubNubTrackerConnection(final Pubnub pubnub, final String roomID) {
        this.pubnub = pubnub;
        this.roomID = roomID;
        try {
            pubnub.setUUID(UUID.randomUUID());
            pubnub.subscribe(roomID, this);
        } catch (PubnubException e) {
            Log.d(e.toString());
        }
    }

    @Override
    public void connectCallback(String channel, Object message) {
        send("onConnected", null, null, null, null);
        Log.d("onConnected!");
    }

    @Override
    public void successCallback(String channel, Object message) {
        // all messages get received here
        if (message instanceof JSONObject) {
            handleMessage((JSONObject) message);
        }
    }

    public void unsubscribe() {
        this.pubnub.unsubscribe(this.roomID);
    }

    @Override
    protected void sendData(JSONObject json) {
        pubnub.publish(this.roomID, json, new Callback() {
        });
    }

    @Override
    public void onScore(Side side, int score, Side nextServer, Side lastServer) {
        send("onScore", side.toString(), score, null, nextServer);
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SENDER_PROPERTY, pubnub.getUUID());
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.LAST_SERVER_PROPERTY, lastServer);
            json.put(JSONInfo.EVENT_PROPERTY, "onScore");
            json.put(JSONInfo.ROLE_PROPERTY, ROLE);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            sendData(json);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + this.roomID + "\n" + ex.getMessage());
        }
    }

    protected void sendTableFramePart(final String encodedFrame, final int index, final int numberOfPackages, boolean doContinue) {
        String encodedFramePart;
        if (index == numberOfPackages - 1) {
            encodedFramePart = encodedFrame.substring(index * MAX_SIZE);
        } else {
            encodedFramePart = encodedFrame.substring(index * MAX_SIZE, (index + 1) * MAX_SIZE);
        }
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SENDER_PROPERTY, pubnub.getUUID());
            json.put(JSONInfo.EVENT_PROPERTY, "onTableFrame");
            json.put(JSONInfo.TABLE_FRAME_INDEX, index);
            json.put(JSONInfo.TABLE_FRAME_NUMBER_OF_PARTS, numberOfPackages);
            json.put(JSONInfo.TABLE_FRAME_WIDTH, this.initTrackerCallback.getCameraWidth());
            json.put(JSONInfo.TABLE_FRAME_HEIGHT, this.initTrackerCallback.getCameraHeight());
            json.put(JSONInfo.TABLE_FRAME, encodedFramePart);
            if (doContinue) {
                pubnub.publish(this.roomID, json, new Callback() {
                    @Override
                    public void successCallback(String channel, Object message) {
                        boolean doContinue = true;
                        initTrackerCallback.updateLoadingBar(index + 2);
                        if (index >= numberOfPackages - 2) {
                            doContinue = false;
                            initTrackerCallback.frameSent();
                        }
                        sendTableFramePart(encodedFrame, index + 1, numberOfPackages, doContinue);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                        Log.d("ERROR_CALLBACK CODE:" + error.getErrorString() + " ERROR_CODE: " + error.errorCode);
                        // TODO display error message -> user should try again
                    }
                });
            } else {
                pubnub.publish(this.roomID, json, new Callback() {
                });
            }
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + this.roomID + "\n" + ex.getMessage());
        }
    }

    protected void send(String event, String side, Integer score, Integer wins, Side nextServer) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SENDER_PROPERTY, pubnub.getUUID());
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.WINS_PROPERTY, wins);
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.ROLE_PROPERTY, ROLE);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            sendData(json);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + this.roomID + "\n" + ex.getMessage());
        }
    }

    public void sendStatusUpdate(String playerNameLeft, String playerNameRight, int scoreLeft, int scoreRight, int winsLeft, int winsRight, Side nextServer, int gamesNeededToWin) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SENDER_PROPERTY, pubnub.getUUID());
            json.put(JSONInfo.PLAYER_NAME_LEFT_PROPERTY, playerNameLeft);
            json.put(JSONInfo.PLAYER_NAME_RIGHT_PROPERTY, playerNameRight);
            json.put(JSONInfo.SCORE_LEFT_PROPERTY, scoreLeft);
            json.put(JSONInfo.SCORE_RIGHT_PROPERTY, scoreRight);
            json.put(JSONInfo.WINS_LEFT_PROPERTY, winsLeft);
            json.put(JSONInfo.WINS_RIGHT_PROPERTY, winsRight);
            json.put(JSONInfo.EVENT_PROPERTY, "onStatusUpdate");
            json.put(JSONInfo.ROLE_PROPERTY, ROLE);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            json.put(JSONInfo.GAMES_NEEDED_PROPERTY, gamesNeededToWin);
            sendData(json);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + this.roomID + "\n" + ex.getMessage());
        }
    }
}
