package cz.fmo;

import android.provider.SyncStateContract;

import com.android.grafika.FrameCallback;
import com.android.grafika.Log;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import cz.fmo.tabletennis.MatchStatus;
import cz.fmo.tabletennis.ScoreManipulationCallback;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.TrackerPubNubCallback;
import cz.fmo.tabletennis.UICallback;
import cz.fmo.util.ByteToBase64Encoder;

public class TrackerPubNub extends Callback implements UICallback {
    private static final String ROLE = "tracker";
    private final Pubnub pubnub;
    private final String roomID;
    private TrackerPubNubCallback callback;
    private ScoreManipulationCallback scoreManipulationCallback;
    private FrameCallback frameCallback;

    public TrackerPubNub(final String roomID, String pubKey, String subKey) {
        this.pubnub = new Pubnub(pubKey, subKey);
        this.roomID = roomID;
        try {
            pubnub.setUUID(UUID.randomUUID());
            pubnub.subscribe(roomID, this);
        } catch (PubnubException e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void connectCallback(String channel, Object message) {
        // send an init message if needed
    }

    @Override
    public void successCallback(String channel, Object message) {
        // all messages get received here
        if (message instanceof JSONObject) {
            handleMessage((JSONObject)message);
        }
    }

    public void setTrackerPubNubCallback(TrackerPubNubCallback callback) {
        this.callback = callback;
    }

    public void setScoreManipulationCallback(ScoreManipulationCallback scoreManipulationCallback) {
        this.scoreManipulationCallback = scoreManipulationCallback;
    }

    public void setFrameCallback(FrameCallback frameCallback) {
        this.frameCallback = frameCallback;
    }

    @Override
    public void onMatchEnded(String winnerName) {
        send("onMatchEnded", winnerName, null,null, null);
    }

    @Override
    public void onScore(Side side, int score, Side nextServer) {
        send("onScore", side.toString(), score,null, nextServer);
    }

    @Override
    public void onWin(Side side, int wins) {
        send("onWin", side.toString(), null, wins, null);
    }

    @Override
    public void onReadyToServe(Side server) {
        send("onReadyToServe", server.toString(), null, null, null);
    }

    private void sendTableFrame(String encodedTableImage) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SENDER_PROPERTY, pubnub.getUUID());
            json.put(JSONInfo.TABLE_FRAME_BYTES, encodedTableImage);
            json.put(JSONInfo.EVENT_PROPERTY, "onTableFrameReceived");
            pubnub.publish(this.roomID, json, new Callback() {});
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel "+this.roomID+"\n"+ex.getMessage());
        }
    }

    private void send(String event, String side, Integer score, Integer wins, Side nextServer) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SENDER_PROPERTY, pubnub.getUUID());
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.WINS_PROPERTY, wins);
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.ROLE_PROPERTY, ROLE);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            pubnub.publish(this.roomID, json, new Callback() {});
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel "+this.roomID+"\n"+ex.getMessage());
        }
    }

    private void sendStatusUpdate(String playerNameLeft, String playerNameRight, int scoreLeft, int scoreRight, int winsLeft, int winsRight, Side nextServer) {
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
            pubnub.publish(this.roomID, json, new Callback() {});
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel "+this.roomID+"\n"+ex.getMessage());
        }
    }

    private void handleMessage(JSONObject json) {
        try {
            String event = json.getString(JSONInfo.EVENT_PROPERTY);
            String side;
            if(event != null) {
                switch (event) {
                    case "onPointDeduction":
                        side = json.getString(JSONInfo.SIDE_PROPERTY);
                        this.scoreManipulationCallback.onPointDeduction(Side.valueOf(side));
                        break;
                    case "onPointAddition":
                        side = json.getString(JSONInfo.SIDE_PROPERTY);
                        this.scoreManipulationCallback.onPointAddition(Side.valueOf(side));
                        break;
                    case "requestStatus":
                        if (this.callback != null) {
                            MatchStatus status = this.callback.onRequestMatchStatus();
                            sendStatusUpdate(status.getPlayerLeft(), status.getPlayerRight(),
                                    status.getScoreLeft(), status.getScoreRight(), status.getWinsLeft(),
                                    status.getWinsRight(), status.getNextServer());
                        }
                        break;
                    case "onPause":
                        this.scoreManipulationCallback.onPause();
                        break;
                    case "onResume":
                        this.scoreManipulationCallback.onResume();
                        break;
                    case "onRequestTableFrame":
                        Log.d("onRequestTableFrame");
                        byte[] frame = this.frameCallback.onCaptureFrame();
                        String encodedFrame = ByteToBase64Encoder.encodeToString(frame);
                        sendTableFrame(encodedFrame);
                        Log.d("frame send: " + frame);
                        break;
                    default:
                        Log.d("Invalid event received.\nevent:"+event);
                        break;
                }
            }
        } catch (JSONException ex) {
            Log.d("Unable to parse JSON from "+this.roomID+"\n"+ex.getMessage());
        }
    }
}
