package cz.fmo;

import android.graphics.Point;

import com.android.grafika.Log;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import cz.fmo.display.DisplayConnectCallback;
import cz.fmo.display.DisplayScoreEventCallback;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.UICallback;
import cz.fmo.util.ByteToBase64Encoder;

public class DisplayPubNub extends Callback {
    private static final String ROLE = "display";
    private final Pubnub pubnub;
    private final String roomID;
    private UICallback uiCallback;
    private DisplayScoreEventCallback scoreCallback;
    private DisplayConnectCallback connectCallback;
    private byte[] frameComplete;
    private int frameCompleteIndex;
    private int numberOfFramePartsReceived;

    public DisplayPubNub(final String roomID, String pubKey, String subKey) {
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
        // send init message if needed
    }

    @Override
    public void successCallback(String channel, Object message) {
        // all messages get received here
        if (message instanceof JSONObject) {
            handleMessage((JSONObject)message);
        }
    }

    public void onStartMatch(Point[] tableCorners) {
        try {
            for(int i = 0; i < tableCorners.length; i++) {
                System.out.println("Point"+ i+ ": "+ tableCorners[i].x + ", " + tableCorners[i].y);
            }
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SENDER_PROPERTY, pubnub.getUUID());
            json.put(JSONInfo.CORNERS, tableCorners);
            json.put(JSONInfo.EVENT_PROPERTY, "onStartMatch");
            pubnub.publish(this.roomID, json, new Callback() {});
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel "+this.roomID+"\n"+ex.getMessage());
        }
    }


    public void requestStatusUpdate() {
        send("requestStatus", null, null, null);
    }

    public void onPointDeduction(Side side) {
        send("onPointDeduction", side.toString(), null,null);
    }

    public void onPointAddition(Side side) {
        send("onPointAddition", side.toString(), null,null);
    }

    public void onRequestTableFrame() {
        send("onRequestTableFrame", null, null, null);
    }

    public void onPause() {
        send("onPause", null, null, null);
    }

    public void onResume() {
        send("onResume", null, null, null);
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

    private void send(String event, String side, Integer score, Integer wins) {
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
        Log.d("onTableFrame");
        int frameSize = json.getInt(JSONInfo.TABLE_FRAME_SIZE);
        int framePartIndex = json.getInt(JSONInfo.TABLE_FRAME_INDEX);
        int numberOfFramePartsSent = json.getInt(JSONInfo.TABLE_FRAME_NUMBER_OF_PARTS);
        byte[] framePart = json.get(JSONInfo.TABLE_FRAME).toString().getBytes();
        Log.d("framePart: "+framePart );
        if (framePartIndex == 0) {
            this.frameComplete = new byte[frameSize];
            this.frameCompleteIndex = 0;
            this.numberOfFramePartsReceived = 1;
            restoreFramePart(framePart, this.frameCompleteIndex, framePart.length);
            this.frameCompleteIndex += framePart.length;
        } else {
            this.numberOfFramePartsReceived++;
            restoreFramePart(framePart, this.frameCompleteIndex, framePart.length);
            this.frameCompleteIndex += framePart.length;
            if (framePartIndex == numberOfFramePartsSent-1) {
                Log.d("number of frame parts sent: " + numberOfFramePartsSent);
                Log.d("number of frame parts received: " + this.numberOfFramePartsReceived);
                if (this.numberOfFramePartsReceived == numberOfFramePartsSent) {
                    //Log.d("encodedFrame length: " + this.encodedFrameComplete.length());
                    //byte[] frame = ByteToBase64Encoder.decodeToByte(this.encodedFrameComplete);
                    Log.d("frame length: " + this.frameComplete.length);
                    this.connectCallback.onImageReceived(this.frameComplete, json.getInt(JSONInfo.TABLE_FRAME_WIDTH), json.getInt(JSONInfo.TABLE_FRAME_HEIGHT));
                } else {
                    onRequestTableFrame();
                }
            }
        }
    }

    private void restoreFramePart(byte[] framePart, int startIndex, int endIndex) {
        for (int i = 0; i < endIndex-startIndex; i++) {
            this.frameComplete[startIndex+i] = framePart[i];
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
                                Side.valueOf(json.getString(JSONInfo.NEXT_SERVER_PROPERTY)));
                        break;
                    case "onWin":
                        this.uiCallback.onWin(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.WINS_PROPERTY)));
                        break;
                    case "onReadyToServe":
                        this.uiCallback.onReadyToServe(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)));
                        break;
                    case "onStatusUpdate":
                        this.scoreCallback.onStatusUpdate(json.getString(JSONInfo.PLAYER_NAME_LEFT_PROPERTY), json.getString(JSONInfo.PLAYER_NAME_RIGHT_PROPERTY),
                                Integer.parseInt(json.getString(JSONInfo.SCORE_LEFT_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.SCORE_RIGHT_PROPERTY)),
                                Integer.parseInt(json.getString(JSONInfo.WINS_LEFT_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.WINS_RIGHT_PROPERTY)),
                                Side.valueOf(json.getString(JSONInfo.NEXT_SERVER_PROPERTY)));
                        break;
                    case "onConnected":
                        this.connectCallback.onConnected();
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
