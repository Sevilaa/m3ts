package ch.m3ts.connection;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import ch.m3ts.connection.pubnub.JSONInfo;
import ch.m3ts.util.Log;
import ch.m3ts.util.Side;
import cz.fmo.data.Track;

public class NearbyTrackerConnection extends ImplTrackerConnection {
    private static final NearbyTrackerConnection instance = new NearbyTrackerConnection();
    private static final String ID = "tracker";
    private static final String JSON_SEND_EXCEPTION_MESSAGE = "Unable to send JSON to endpoint ";
    private ConnectionsClient connection;
    private EndpointDiscoveryCallback endpointDiscoveryCallback;
    private ConnectionLifecycleCallback connectionLifecycleCallback;
    private PayloadCallback payloadCallback;
    private ConnectionCallback connectionCallback;
    private String advertiserEndpointID = "";
    private String endpointName = "";
    private udpClient UDPClient;

    private NearbyTrackerConnection() {
    }

    public static NearbyTrackerConnection getInstance() {
        return instance;
    }

    public void init(Context context) {
        this.UDPClient = new udpClient("192.168.1.250");
        this.connection = Nearby.getConnectionsClient(context.getApplicationContext());
        initCallbacks();
    }

    public void startDiscovery() {
        this.connection.stopAllEndpoints();
        this.connection.stopAdvertising();
        this.connection.stopDiscovery();
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
        connection.startDiscovery(ConnectionHelper.SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        unused -> {
                            // We're discovering!
                        })
                .addOnFailureListener(
                        e -> {
                            // We're unable to start discovering.
                        });
    }

    private void initCallbacks() {
        this.payloadCallback = new PayloadCallback() {

            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                try {
                    JSONObject json = new JSONObject(new String(payload.asBytes(), UTF_8));
                    handleMessage(json);
                } catch (JSONException ex) {
                    Log.d("Unable to receive JSON from endpoint \n" + ex.getMessage());
                }
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                // no implementation needed
            }
        };

        this.endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                // An endpoint was found. We request a connection to it.
                advertiserEndpointID = s;
                connection.requestConnection(ID, advertiserEndpointID, connectionLifecycleCallback)
                        .addOnSuccessListener(
                                unused -> {
                                    // We successfully requested a connection. Now both sides
                                    // must accept before the connection is established.
                                })
                        .addOnFailureListener(
                                e -> {
                                    // Nearby Connections failed to request the connection.
                                    Log.d(e.getMessage() + "");
                                });
            }

            @Override
            public void onEndpointLost(String endpointId) {
                // A previously discovered endpoint has gone away.
            }
        };

        this.connectionLifecycleCallback = new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(final String endpointId, ConnectionInfo connectionInfo) {
                endpointName = connectionInfo.getEndpointName();
                connectionCallback.onConnecting(endpointId, endpointName, connectionInfo.getAuthenticationToken(), payloadCallback);
            }

            @Override
            public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
                if (connectionResolution.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
                    connection.stopDiscovery();
                    advertiserEndpointID = s;
                    connectionCallback.onConnection(endpointName);
                } else {
                    connectionCallback.onRejection();
                }
            }

            @Override
            public void onDisconnected(String endpointId) {
                // We've been disconnected from this endpoint. No more data can be
                // sent or received.
                if (connectionCallback != null) {
                    connectionCallback.onDisconnection(endpointName);
                }
            }
        };
    }

    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }


    protected void send(String event, String side, Integer score, Integer wins, Side nextServer) {
        Log.d("Sending in NearbyTrackerConnection.java");
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.WINS_PROPERTY, wins);
            json.put(JSONInfo.EVENT_PROPERTY, event);
            Log.d("Sending UDP");
            UDPClient.sendString("Event happened!");
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            //UDPClient.sendData(json);
            sendData(json);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + this.advertiserEndpointID + "\n" + ex.getMessage());
        }
    }

    @Override
    protected void sendData(JSONObject json) {
        Payload payload = Payload.fromBytes(json.toString().getBytes(UTF_8));
        if (this.connection != null)
            this.connection.sendPayload(this.advertiserEndpointID, payload);    // needed Null Check for testing in LiveActivity...
    }

    @Override
    protected void sendPart(JSONObject json, int index, int numberOfPackages, String encodedData) {
        sendData(json);
        boolean doContinue = true;
        if (index >= numberOfPackages - 2) {
            doContinue = false;
        }
        createPart(json, encodedData, index + 1, numberOfPackages, doContinue);
    }

    @Override
    public void sendTrack(Track track) {
        //Only needed with Hololens where PubNub is used
    }
}
