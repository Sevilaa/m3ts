package cz.fmo;

import android.graphics.ImageFormat;

import com.android.grafika.Log;
import com.android.grafika.tracker.InitTrackerCallback;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import cz.fmo.tabletennis.MatchStatus;
import cz.fmo.tabletennis.ScoreManipulationCallback;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.TrackerPubNubCallback;
import cz.fmo.tabletennis.UICallback;
import cz.fmo.util.ByteToBase64Encoder;
import helper.CameraBytesConversions;

public class TrackerPubNub extends Callback implements UICallback {
    private static final int CAMERA_FORMAT = ImageFormat.NV21;
    private static final String ROLE = "tracker";
    private static final int MAX_SIZE = 10000;
    private final Pubnub pubnub;
    private final String roomID;
    private TrackerPubNubCallback callback;
    private ScoreManipulationCallback scoreManipulationCallback;
    private InitTrackerCallback initTrackerCallback;

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
        send("onConnected", null, null, null, null);
    }

    @Override
    public void successCallback(String channel, Object message) {
        // all messages get received here
        if (message instanceof JSONObject) {
            handleMessage((JSONObject)message);
        }
    }

    public void unsubscribe() {
        this.pubnub.unsubscribe(this.roomID);
    }

    public void setTrackerPubNubCallback(TrackerPubNubCallback callback) {
        this.callback = callback;
    }

    public void setScoreManipulationCallback(ScoreManipulationCallback scoreManipulationCallback) {
        this.scoreManipulationCallback = scoreManipulationCallback;
    }

    public void setInitTrackerCallback(InitTrackerCallback initTrackerCallback) {
        this.initTrackerCallback = initTrackerCallback;
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

    private void sendTableFramePart(final String encodedFrame, final int index, final int numberOfPackages, boolean doContinue) {
        String encodedFramePart;
        if (index == numberOfPackages-1) {
            encodedFramePart = encodedFrame.substring(index*MAX_SIZE);
        } else {
            encodedFramePart = encodedFrame.substring(index*MAX_SIZE, (index+1)*MAX_SIZE);
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
                        initTrackerCallback.updateLoadingBar(index+2);
                        if (index == numberOfPackages-2) {
                            doContinue = false;
                            initTrackerCallback.frameSent();
                        }
                        sendTableFramePart(encodedFrame, index + 1, numberOfPackages, doContinue);
                    }
                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                        Log.d("ERROR_CALLBACK CODE:"+error.getErrorString()+" ERROR_CODE: " +error.errorCode);
                        // TODO display error message -> user should try again
                    }
                });
            } else {
                pubnub.publish(this.roomID, json, new Callback() {});
            }
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

    public void sendStatusUpdate(String playerNameLeft, String playerNameRight, int scoreLeft, int scoreRight, int winsLeft, int winsRight, Side nextServer) {
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

    private void handleOnRequestTableFrame() {
        Log.d("onRequestTableFrame");
        byte[] frame = this.initTrackerCallback.onCaptureFrame();
        byte[] compressedJPGBytes = CameraBytesConversions.compressCameraImageBytes(frame, this.initTrackerCallback.getCameraWidth(), this.initTrackerCallback.getCameraHeight());
        Log.d("frame length of compressed image: " + compressedJPGBytes.length + "Bytes");
        try {
            String encodedFrame = ByteToBase64Encoder.encodeToString(compressedJPGBytes);
            Log.d("encodedFrame length: " + encodedFrame.length());
            sendTableFrame(encodedFrame);
        } catch (Exception ex) {
            Log.d("UNSUPPORTED ENCODING EXCEPTION:");
            Log.d(ex.getMessage());
        }
    }

    private void sendTableFrame(String encodedFrame) {
        int numberOfPackages = (int)Math.ceil(encodedFrame.length() / (double) MAX_SIZE);
        Log.d("numberOfPackages: " + numberOfPackages);
        this.initTrackerCallback.setLoadingBarSize(numberOfPackages);
        sendTableFramePart(encodedFrame, 0, numberOfPackages, true);
    }

    private void handleOnSelectTableCorner(JSONArray tableCorners) {
        Log.d("onTableCorner: " + tableCorners);
        if (tableCorners != null) {
            int[] coordinates = new int[tableCorners.length()];

            for (int i = 0; i < tableCorners.length(); ++i) {
                coordinates[i] = tableCorners.optInt(i);
            }
            this.initTrackerCallback.setTableCorners(coordinates);
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
                        handleOnRequestTableFrame();
                        break;
                    case "onSelectTableCorner":
                        JSONArray tableCorners = json.getJSONArray(JSONInfo.CORNERS);
                        handleOnSelectTableCorner(tableCorners);
                        break;
                    case "onStartMatch":
                        this.initTrackerCallback.switchToDebugActivity();
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
