package ch.m3ts.connection.pubnub;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import ch.m3ts.Log;
import ch.m3ts.connection.ImplDisplayConnection;

public class PubNubDisplayConnection extends ImplDisplayConnection {
    private final Pubnub pubnub;
    private final String roomID;

    public PubNubDisplayConnection(final Pubnub pubnub, final String roomID) {
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
            handleMessage((JSONObject) message);
        }
    }

    public void unsubscribe() {
        this.pubnub.unsubscribe(this.roomID);
    }

    public void onStartMatch(String matchType, String server) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.TYPE_PROPERTY, matchType);
            json.put(JSONInfo.SERVER_PROPERTY, server);
            json.put(JSONInfo.EVENT_PROPERTY, "onStartMatch");
            sendData(json);
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel " + this.roomID + "\n" + ex.getMessage());
        }
    }

    public String getRoomID() {
        return roomID;
    }

    protected void sendData(JSONObject json) {
        pubnub.publish(this.roomID, json, new Callback() {
        });
    }

    protected void send(String event, String side) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.SIDE_PROPERTY, side);
            sendData(json);
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to channel " + this.roomID + "\n" + ex.getMessage());
        }
    }
}
