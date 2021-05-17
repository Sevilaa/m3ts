package ch.m3ts.connection;

import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

import ch.m3ts.connection.pubnub.JSONInfo;
import ch.m3ts.util.Log;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NearbyDisplayConnection extends ImplDisplayConnection {
    private static final NearbyDisplayConnection instance = new NearbyDisplayConnection();
    private final String ID = "display";
    private ConnectionLifecycleCallback connectionLifecycleCallback;
    private PayloadCallback payloadCallback;
    private ConnectionsClient connection;
    private String discovererEndpointID = "";
    private ConnectionCallback connectionCallback;
    private String endpointName;

    private NearbyDisplayConnection() {
    }

    public static NearbyDisplayConnection getInstance() {
        return instance;
    }

    public void init(Context context) {
        this.connection = Nearby.getConnectionsClient(context.getApplicationContext());
        initCallbacks();
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

            }
        };
        this.connectionLifecycleCallback = new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
                if (connectionResolution.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
                    connection.stopAdvertising();
                    discovererEndpointID = s;
                    connectionCallback.onConnection(endpointName);
                    displayConnectCallback.onImageTransmissionStarted(100);
                } else {
                    connectionCallback.onRejection();
                }
            }

            @Override
            public void onConnectionInitiated(@NonNull final String endpointId, @NonNull ConnectionInfo connectionInfo) {
                endpointName = connectionInfo.getEndpointName();
                connectionCallback.onConnecting(endpointId, endpointName, connectionInfo.getAuthenticationToken(), payloadCallback);
            }

            @Override
            public void onDisconnected(String endpointId) {
                if (connectionCallback != null) {
                    connectionCallback.onDisconnection(endpointName);
                }
            }
        };
    }

    public void startAdvertising() {
        this.connection.stopAllEndpoints();
        this.connection.stopAdvertising();
        this.connection.stopDiscovery();
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
        this.connection
                .startAdvertising(
                        this.ID, ConnectionHelper.SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
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

    public void setConnectCallback(ConnectionCallback callback) {
        this.connectionCallback = callback;
    }

    protected void send(String event, String side) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.SIDE_PROPERTY, side);
            sendData(json);
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to endpoint " + this.discovererEndpointID + "\n" + ex.getMessage());
        }
    }

    protected void sendData(JSONObject json) {
        Payload payload = Payload.fromBytes(json.toString().getBytes(UTF_8));
        this.connection.sendPayload(this.discovererEndpointID, payload);
    }
}
