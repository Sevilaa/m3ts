package cz.fmo;

import com.android.grafika.Log;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import cz.fmo.display.DisplayEventCallback;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.UICallback;

public class DisplayPubNub extends Callback {
    private static final String ROLE = "display";
    private final Pubnub pubnub;
    private final String roomID;
    private UICallback callback;
    private DisplayEventCallback displayEventCallback;

    public DisplayPubNub(final String roomID, String pubKey, String subKey, UICallback callback, DisplayEventCallback displayEventCallback) {
        this.pubnub = new Pubnub(pubKey, subKey);
        this.roomID = roomID;
        this.callback = callback;
        this.displayEventCallback = displayEventCallback;
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
        send("requestStatus", null, null, null);
    }

    @Override
    public void successCallback(String channel, Object message) {
        // all messages get received here
        if (message instanceof JSONObject) {
            handleMessage((JSONObject)message);
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

    private void handleMessage(JSONObject json) {
        try {
            String event = json.getString(JSONInfo.EVENT_PROPERTY);
            if(event != null) {
                switch (event) {
                    case "onMatchEnded":
                        this.callback.onMatchEnded(json.getString(JSONInfo.SIDE_PROPERTY));
                        break;
                    case "onScore":
                        this.callback.onScore(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.SCORE_PROPERTY)));
                        break;
                    case "onWin":
                        this.callback.onWin(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.WINS_PROPERTY)));
                        break;
                    case "onStatusUpdate":
                        this.displayEventCallback.onStatusUpdate(json.getString(JSONInfo.PLAYER_NAME_LEFT_PROPERTY), json.getString(JSONInfo.PLAYER_NAME_RIGHT_PROPERTY),
                                Integer.parseInt(json.getString(JSONInfo.SCORE_LEFT_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.SCORE_RIGHT_PROPERTY)),
                                Integer.parseInt(json.getString(JSONInfo.WINS_LEFT_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.WINS_RIGHT_PROPERTY)));
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
