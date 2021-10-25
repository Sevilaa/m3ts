package ch.m3ts.connection.pubnub;

import android.graphics.Color;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import ch.m3ts.connection.ConnectionEvent;
import ch.m3ts.connection.ImplTrackerConnection;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.util.Log;
import ch.m3ts.util.Side;
import cz.fmo.Lib;
import cz.fmo.data.Track;

public class PubNubTrackerConnection extends ImplTrackerConnection {
    private static final String JSON_SEND_EXCEPTION_MESSAGE = "Unable to send JSON to channel ";
    private final Pubnub pubnub;
    private final String roomID;

    public PubNubTrackerConnection(final Pubnub pubnub, final String roomID) {
        this.pubnub = pubnub;
        this.roomID = roomID;
        TTEventBus.getInstance().register(this);
        try {
            pubnub.setUUID(UUID.randomUUID());
            pubnub.subscribe(roomID, this);
        } catch (PubnubException e) {
            Log.d(e.toString());
        }
    }

    @Override
    public void connectCallback(String channel, Object message) {
        send(ConnectionEvent.CONNECTION, null, null, null, null);
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
    protected void sendPart(final JSONObject json, final int index, final int numberOfPackages, final String encodedData) {
        pubnub.publish(this.roomID, json, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                boolean doContinue = true;
                String event = "";
                try {
                    event = json.getString(JSONInfo.EVENT_PROPERTY);
                } catch (JSONException e) {
                    Log.d("failed to update loading bar");
                }
                sentPart(event, index + 2);
                if (index >= numberOfPackages - 2) {
                    doContinue = false;
                    sentMultipartCompletely(event);
                }
                createPart(json, encodedData, index + 1, numberOfPackages, doContinue);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                Log.d("ERROR_CALLBACK CODE:" + error.getErrorString() + " ERROR_CODE: " + error.errorCode);
                // TODO display error message -> user should try again
            }
        });
    }

    protected void send(String event, String side, Integer score, Integer wins, Side nextServer) {
        try {
            JSONObject json = new JSONObject();
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

    @Override
    public void sendTrack(Track track) {
        if(track == null){
            return;
        }
        Lib.Detection latest = track.getLatest();
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        while (latest != null){
            int color = Color.rgb(Math.round(track.getColor().rgba[0]), Math.round(track.getColor().rgba[1]), Math.round(track.getColor().rgba[2]));
            try {
                array.put(latest.centerX);
                array.put(latest.centerY);
                array.put(latest.centerZ);
                array.put(color);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            latest = latest.predecessor;
        }
        try {
            json.put("Track", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendData(json);
    }
}
