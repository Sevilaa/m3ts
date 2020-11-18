package cz.fmo;

import com.android.grafika.Log;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import cz.fmo.tabletennis.ScoreManipulationCallback;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.UICallback;

public class TrackerPubNub implements UICallback {
    private static final String ROLE = "tracker";
    private final Pubnub pubnub;
    private final String roomID;
    private ScoreManipulationCallback scoreManipulationCallback;

    public TrackerPubNub(final String roomID, String pubKey, String subKey) {
        this.pubnub = new Pubnub(pubKey, subKey);
        this.roomID = roomID;
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

    public void setScoreManipulationCallback(ScoreManipulationCallback scoreManipulationCallback) {
        this.scoreManipulationCallback = scoreManipulationCallback;
    }

    @Override
    public void onMatchEnded() {
        send("onMatchEnded", null, null,null);
    }

    @Override
    public void onScore(Side side, int score) {
        send("onScore", side.toString(), score,null);
    }

    @Override
    public void onWin(Side side, int wins) {
        send("onWin", side.toString(), null, wins);
    }

    private void send(String event, String side, Integer score, Integer wins) {
        try {
            JSONObject json = new JSONObject();
            json.put("sender", pubnub.getUUID());
            json.put("side", side);
            json.put("score", score);
            json.put("wins", wins);
            json.put("event", event);
            json.put("role", ROLE);
            pubnub.publish(this.roomID, json, new Callback() {});
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel "+this.roomID+"\n"+ex.getMessage());
        }
    }

    private void handleMessage(JSONObject json) {
        try {
            String event = json.getString("event");
            String side = json.getString("side");
            if(event != null && side != null) {
                switch (event) {
                    case "onPointDeduction":
                        this.scoreManipulationCallback.onPointDeduction(Side.valueOf(side));
                        break;
                    case "onPointAddition":
                        this.scoreManipulationCallback.onPointAddition(Side.valueOf(side));
                        break;
                    default:
                        Log.d("Invalid side or event received.\nevent:"+event+" side:"+side);
                        break;
                }
            }
        } catch (JSONException ex) {
            Log.d("Unable to parse JSON from "+this.roomID+"\n"+ex.getMessage());
        }
    }
}
