package cz.fmo;

import com.android.grafika.Log;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.UICallback;

public class DisplayPubNub {
    private static final String ROLE = "display";
    private final Pubnub pubnub;
    private final String roomID;
    private UICallback callback;

    public DisplayPubNub(final String roomID, String pubKey, String subKey, UICallback callback) {
        this.pubnub = new Pubnub(pubKey, subKey);
        this.roomID = roomID;
        this.callback = callback;
        try {
            pubnub.setUUID(UUID.randomUUID());
            pubnub.subscribe(roomID, new Callback() {
                        @Override
                        public void connectCallback(String channel, Object message) {
                            // send init message if needed
                            send("hello gaymers", null, null, null);
                        }

                        @Override
                        public void disconnectCallback(String channel, Object message) {
                            // don't care
                            System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        public void reconnectCallback(String channel, Object message) {
                            // don't care
                            System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            // all messages get received here
                            if (message instanceof JSONObject) {
                                handleMessage((JSONObject)message);
                            }
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            // don't care
                            System.out.println("SUBSCRIBE : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );
        } catch (PubnubException e) {
            System.out.println(e.toString());
        }
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
            json.put("sender", pubnub.getUUID());
            json.put("event", event);
            json.put("side", side);
            json.put("role", ROLE);
            pubnub.publish(this.roomID, json, new Callback() {});
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel "+this.roomID+"\n"+ex.getMessage());
        }
    }

    private void handleMessage(JSONObject json) {
        try {
            String event = json.getString("event");
            if(event != null) {
                switch (event) {
                    case "onMatchEnded":
                        this.callback.onMatchEnded();
                        break;
                    case "onScore":
                        this.callback.onScore(Side.valueOf(json.getString("side")), Integer.parseInt(json.getString("score")));
                        break;
                    case "onWin":
                        this.callback.onWin(Side.valueOf(json.getString("side")), Integer.parseInt(json.getString("wins")));
                        break;
                    default:
                        Log.d("Invalid side or event received.");
                        break;
                }
            }
        } catch (Exception ex) {
            Log.d("Unable to parse JSON from "+this.roomID+"\n"+ex.getMessage());
        }
    }

}
