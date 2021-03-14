package ch.m3ts.pubnub;

import android.graphics.Point;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import ch.m3ts.Log;
import ch.m3ts.display.DisplayConnectCallback;
import ch.m3ts.display.DisplayScoreEventCallback;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.UICallback;

public class DisplayPubNub extends Callback {
    private static final String ROLE = "display";
    private final Pubnub pubnub;
    private final String roomID;
    private UICallback uiCallback;
    private DisplayScoreEventCallback scoreCallback;
    private DisplayConnectCallback connectCallback;
    private String encodedFrameComplete;
    private int numberOfEncodedFrameParts;

    public DisplayPubNub(final Pubnub pubnub, final String roomID) {
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
        // send init message if needed
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

    public void onSelectTableCorners(Point[] tableCorners) {
        try {
            int[] corners = new int[tableCorners.length*2];
            for(int i = 0; i < tableCorners.length; i++) {
                Log.d("Point"+ i+ ": "+ tableCorners[i].x + ", " + tableCorners[i].y);
                corners[2*i] = tableCorners[i].x;
                corners[2*i+1] = tableCorners[i].y;
            }
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SENDER_PROPERTY, pubnub.getUUID());
            json.put(JSONInfo.CORNERS, new JSONArray(corners));
            json.put(JSONInfo.EVENT_PROPERTY, "onSelectTableCorner");
            pubnub.publish(this.roomID, json, new Callback() {});
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel "+this.roomID+"\n"+ex.getMessage());
        }
    }

    public void onStartMatch() { send("onStartMatch", null); }

    public void onRestartMatch() { send("onRestartMatch", null); }

    public void requestStatusUpdate() {
        send("requestStatus", null);
    }

    public void onPointDeduction(Side side) {
        send("onPointDeduction", side.toString());
    }

    public void onPointAddition(Side side) {
        send("onPointAddition", side.toString());
    }

    public void onRequestTableFrame() {
        send("onRequestTableFrame", null);
    }

    public void onPause() {
        send("onPause", null);
    }

    public void onResume() {
        send("onResume", null);
    }

    public void setUiCallback(UICallback uiCallback) {
        this.uiCallback = uiCallback;
    }

    public void setDisplayScoreEventCallback(DisplayScoreEventCallback displayScoreCallback) {
        this.scoreCallback = displayScoreCallback;
    }

    public void setDisplayConnectCallback(DisplayConnectCallback displayConnectCallback) {
        this.connectCallback = displayConnectCallback;
    }

    public String getRoomID() {
        return roomID;
    }

    private void send(String event, String side) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SENDER_PROPERTY, pubnub.getUUID());
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.ROLE_PROPERTY, ROLE);
            pubnub.publish(this.roomID, json, new Callback() {});
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel "+this.roomID+"\n"+ex.getMessage());
        }
    }

    private void handleOnTableFrame(JSONObject json) throws JSONException {
        int encodedFramePartIndex = json.getInt(JSONInfo.TABLE_FRAME_INDEX);
        int numberOfFramePartsSent = json.getInt(JSONInfo.TABLE_FRAME_NUMBER_OF_PARTS);
        String encodedFramePart = json.getString(JSONInfo.TABLE_FRAME);
        if (encodedFramePartIndex == 0) {
            this.numberOfEncodedFrameParts = 1;
            this.encodedFrameComplete = encodedFramePart;
            this.connectCallback.onImageTransmissionStarted(numberOfFramePartsSent);
        } else {
            this.numberOfEncodedFrameParts++;
            this.encodedFrameComplete += encodedFramePart;
            this.connectCallback.onImagePartReceived(encodedFramePartIndex+1);
            if (encodedFramePartIndex == numberOfFramePartsSent-1) {
                Log.d("number of frame parts sent: " + numberOfFramePartsSent);
                Log.d("number of frame parts received: " + numberOfEncodedFrameParts);
                if (this.numberOfEncodedFrameParts == numberOfFramePartsSent) {
                    Log.d("encodedFrame length: " + this.encodedFrameComplete.length());
                    byte[] frame = ByteToBase64.decodeToByte(this.encodedFrameComplete);
                    Log.d("frame length: " + frame.length);
                    this.connectCallback.onImageReceived(frame, json.getInt(JSONInfo.TABLE_FRAME_WIDTH), json.getInt(JSONInfo.TABLE_FRAME_HEIGHT));
                } else {
                    onRequestTableFrame();
                }
            }
        }
    }

    private void handleMessage(JSONObject json) {
        try {
            String event = json.getString(JSONInfo.EVENT_PROPERTY);
            if(event != null) {
                switch (event) {
                    case "onMatchEnded":
                        this.uiCallback.onMatchEnded(json.getString(JSONInfo.SIDE_PROPERTY));
                        break;
                    case "onScore":
                        this.uiCallback.onScore(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.SCORE_PROPERTY)),
                                Side.valueOf(json.getString(JSONInfo.NEXT_SERVER_PROPERTY)), Side.valueOf(json.getString(JSONInfo.LAST_SERVER_PROPERTY)));
                        break;
                    case "onWin":
                        this.uiCallback.onWin(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.WINS_PROPERTY)));
                        break;
                    case "onReadyToServe":
                        this.uiCallback.onReadyToServe(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)));
                        break;
                    case "onNotReadyButPlaying":
                        this.uiCallback.onNotReadyButPlaying();
                        break;
                    case "onStatusUpdate":
                        this.scoreCallback.onStatusUpdate(json.getString(JSONInfo.PLAYER_NAME_LEFT_PROPERTY), json.getString(JSONInfo.PLAYER_NAME_RIGHT_PROPERTY),
                                Integer.parseInt(json.getString(JSONInfo.SCORE_LEFT_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.SCORE_RIGHT_PROPERTY)),
                                Integer.parseInt(json.getString(JSONInfo.WINS_LEFT_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.WINS_RIGHT_PROPERTY)),
                                Side.valueOf(json.getString(JSONInfo.NEXT_SERVER_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.GAMES_NEEDED_PROPERTY)));
                        break;
                    case "onConnected":
                        if(this.connectCallback != null) {
                            this.connectCallback.onConnected();
                        }
                        break;
                    case "onTableFrame":
                        this.handleOnTableFrame(json);
                        break;
                    default:
                        Log.d("Unhandled event received:\n"+json.toString());
                        break;
                }
            }
        } catch (Exception ex) {
            Log.d("Unable to parse JSON from "+this.roomID+"\n"+ex.getMessage());
        }
    }

}
