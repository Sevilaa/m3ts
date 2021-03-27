package ch.m3ts.connection;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;

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

import ch.m3ts.Log;
import ch.m3ts.connection.pubnub.JSONInfo;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.UICallback;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NearbyTrackerConnection extends ImplTrackerConnection implements UICallback {
    private static final NearbyTrackerConnection instance = new NearbyTrackerConnection();
    private static final String ID = "tracker";
    private Context context;
    private ConnectionsClient connection;
    private EndpointDiscoveryCallback endpointDiscoveryCallback;
    private ConnectionLifecycleCallback connectionLifecycleCallback;
    private PayloadCallback payloadCallback;
    private ConnectionCallback connectionCallback;
    private String advertiserEndpointID = "";
    private static final String JSON_SEND_EXCEPTION_MESSAGE = "Unable to send JSON to endpoint ";
    private String endpointName = "";

    private NearbyTrackerConnection() {
    }

    public static NearbyTrackerConnection getInstance() {
        return instance;
    }

    public void init(Context context) {
        this.context = context;
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
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // We're discovering!
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We're unable to start discovering.
                            }
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
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // We successfully requested a connection. Now both sides
                                        // must accept before the connection is established.
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Nearby Connections failed to request the connection.
                                        Log.d(e.getMessage() + "");
                                    }
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
                connectionCallback.onConnecting(endpointName);
                AlertDialog dialog = ConnectionHelper.makeAuthenticationDialog(context, endpointId, endpointName, connectionInfo.getAuthenticationToken(), payloadCallback);
                dialog.show();
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
                if(connectionCallback != null) {
                    connectionCallback.onDisconnection(endpointName);
                }
            }
        };
    }

    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    @Override
    public void onScore(Side side, int score, Side nextServer, Side lastServer) {
        send("onScore", side.toString(), score,null, nextServer);
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.LAST_SERVER_PROPERTY, lastServer);
            json.put(JSONInfo.EVENT_PROPERTY, "onScore");
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            Payload payload = Payload.fromBytes(json.toString().getBytes(UTF_8));
            this.connection.sendPayload(this.advertiserEndpointID, payload);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE+this.advertiserEndpointID+"\n"+ex.getMessage());
        }
    }

    protected void send(String event, String side, Integer score, Integer wins, Side nextServer) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.WINS_PROPERTY, wins);
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            sendData(json);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + this.advertiserEndpointID + "\n" + ex.getMessage());
        }
    }

    @Override
    protected void sendData(JSONObject json) {
        Payload payload = Payload.fromBytes(json.toString().getBytes(UTF_8));
        this.connection.sendPayload(this.advertiserEndpointID, payload);
    }

    public void sendStatusUpdate(String playerNameLeft, String playerNameRight, int scoreLeft, int scoreRight, int winsLeft, int winsRight, Side nextServer, int gamesNeededToWin) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.PLAYER_NAME_LEFT_PROPERTY, playerNameLeft);
            json.put(JSONInfo.PLAYER_NAME_RIGHT_PROPERTY, playerNameRight);
            json.put(JSONInfo.SCORE_LEFT_PROPERTY, scoreLeft);
            json.put(JSONInfo.SCORE_RIGHT_PROPERTY, scoreRight);
            json.put(JSONInfo.WINS_LEFT_PROPERTY, winsLeft);
            json.put(JSONInfo.WINS_RIGHT_PROPERTY, winsRight);
            json.put(JSONInfo.EVENT_PROPERTY, "onStatusUpdate");
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            json.put(JSONInfo.GAMES_NEEDED_PROPERTY, gamesNeededToWin);
            sendData(json);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE+this.advertiserEndpointID+"\n"+ex.getMessage());
        }
    }

    protected void sendTableFramePart(final String encodedFrame, final int index, final int numberOfPackages, boolean doContinue) {
        String encodedFramePart;
        if (index == numberOfPackages - 1) {
            encodedFramePart = encodedFrame.substring(index * MAX_SIZE);
        } else {
            encodedFramePart = encodedFrame.substring(index * MAX_SIZE, (index + 1) * MAX_SIZE);
        }
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.EVENT_PROPERTY, "onTableFrame");
            json.put(JSONInfo.TABLE_FRAME_INDEX, index);
            json.put(JSONInfo.TABLE_FRAME_NUMBER_OF_PARTS, numberOfPackages);
            json.put(JSONInfo.TABLE_FRAME_WIDTH, this.initTrackerCallback.getCameraWidth());
            json.put(JSONInfo.TABLE_FRAME_HEIGHT, this.initTrackerCallback.getCameraHeight());
            json.put(JSONInfo.TABLE_FRAME, encodedFramePart);
            Payload payload = Payload.fromBytes(json.toString().getBytes(UTF_8));
            if (doContinue) {
                this.connection.sendPayload(this.advertiserEndpointID, payload);
                doContinue = true;
                initTrackerCallback.updateLoadingBar(index + 2);
                if (index >= numberOfPackages - 2) {
                    doContinue = false;
                }
                sendTableFramePart(encodedFrame, index + 1, numberOfPackages, doContinue);
            } else {
                this.connection.sendPayload(this.advertiserEndpointID, payload);
            }
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE+this.advertiserEndpointID+"\n"+ex.getMessage());
        }
    }
}
