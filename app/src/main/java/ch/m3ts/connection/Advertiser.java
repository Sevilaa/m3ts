package ch.m3ts.connection;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.m3ts.Log;
import ch.m3ts.display.DisplayConnectCallback;
import ch.m3ts.display.DisplayScoreEventCallback;
import ch.m3ts.pubnub.ByteToBase64;
import ch.m3ts.pubnub.JSONInfo;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.UICallback;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Advertiser {
    private ConnectionLifecycleCallback connectionLifecycleCallback;
    private PayloadCallback payloadCallback;
    private Context context;
    private ConnectionsClient connection;
    private final String ID = "display";
    private String discovererEndpointID = "";
    private UICallback uiCallback;
    private DisplayScoreEventCallback scoreCallback;
    private DisplayConnectCallback connectCallback;
    private String encodedFrameComplete;
    private int numberOfEncodedFrameParts;


    public Advertiser(Context context) {
        this.context = context;
        this.connection = Nearby.getConnectionsClient(context);
        initCallbacks();
        startAdvertising();
    }

    private void initCallbacks() {
        this.payloadCallback = new PayloadCallback() {

            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                handleMessage(payload);
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

            }
        };
         this.connectionLifecycleCallback = new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
                switch (connectionResolution.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        connection.stopDiscovery();
                        connection.stopAdvertising();
                        discovererEndpointID = s;
                        // We're connected! Can now start sending and receiving data.
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        break;
                    default:
                        // Unknown status code
                }
            }

            @Override
            public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
                // Automatically accept the connection on both sides.
                connection.acceptConnection(s, payloadCallback);
            }

            @Override
            public void onDisconnected(String endpointId) {
                // We've been disconnected from this endpoint. No more data can be
                // sent or received.
            }
        };
    }

    private void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
        this.connection
                .startAdvertising(
                        this.ID, context.getPackageName(), connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // We're advertising!
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start advertising.
                            }
                        });
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
            json.put(JSONInfo.CORNERS, new JSONArray(corners));
            json.put(JSONInfo.EVENT_PROPERTY, "onSelectTableCorner");
            Payload payload = Payload.fromBytes(json.toString().getBytes(UTF_8));
            this.connection.sendPayload(this.discovererEndpointID, payload);
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to endpoint "+this.discovererEndpointID+"\n"+ex.getMessage());
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

    private void send(String event, String side) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.SIDE_PROPERTY, side);
            Payload payload = Payload.fromBytes(json.toString().getBytes(UTF_8));
            this.connection.sendPayload(this.discovererEndpointID, payload);
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to endpoint "+this.discovererEndpointID+"\n"+ex.getMessage());
        }
    }

    private void handleMessage(Payload payload) {
        try {
            //new String(payload.asBytes(), UTF_8)
            // Payload test = Payload.fromBytes(json.toString().getBytes(UTF_8));
            // JSONObject json2 = new JSONObject();
            // json2.put("test", new String(test.asBytes(), UTF_8))
            JSONObject json = new JSONObject(new String(payload.asBytes(), UTF_8));
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
            Log.d("Unable to send JSON to endpoint "+this.discovererEndpointID+"\n"+ex.getMessage());
        }
    }
}
