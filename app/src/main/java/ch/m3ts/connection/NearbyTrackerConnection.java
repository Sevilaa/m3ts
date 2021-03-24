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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.m3ts.Log;
import ch.m3ts.pubnub.ByteToBase64;
import ch.m3ts.pubnub.CameraBytesConversions;
import ch.m3ts.pubnub.JSONInfo;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchStatus;
import ch.m3ts.tabletennis.match.MatchStatusCallback;
import ch.m3ts.tabletennis.match.UICallback;
import ch.m3ts.tabletennis.match.game.ScoreManipulationCallback;
import ch.m3ts.tracker.init.InitTrackerCallback;
import ch.m3ts.tracker.visualization.MatchVisualizeHandlerCallback;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NearbyTrackerConnection implements UICallback, TrackerConnection {
    private static final NearbyTrackerConnection instance = new NearbyTrackerConnection();
    private Context context;
    private ConnectionsClient connection;
    private EndpointDiscoveryCallback endpointDiscoveryCallback;
    private ConnectionLifecycleCallback connectionLifecycleCallback;
    private PayloadCallback payloadCallback;
    private final String ID = "tracker";
    private String advertiserEndpointID = "";
    private MatchStatusCallback callback;
    private ScoreManipulationCallback scoreManipulationCallback;
    private InitTrackerCallback initTrackerCallback;
    private ConnectionCallback connectionCallback;
    private MatchVisualizeHandlerCallback matchVisualizeHandlerCallback;
    private static final int MAX_SIZE = 1000;
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
                handleMessage(payload);
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

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

    public void setTrackerPubNubCallback(MatchStatusCallback callback) {
        this.callback = callback;
    }

    public void setScoreManipulationCallback(ScoreManipulationCallback scoreManipulationCallback) {
        this.scoreManipulationCallback = scoreManipulationCallback;
    }

    public void setInitTrackerCallback(InitTrackerCallback initTrackerCallback) {
        this.initTrackerCallback = initTrackerCallback;
    }

    public void setMatchVisualizeHandlerCallback(MatchVisualizeHandlerCallback matchVisualizeHandlerCallback) {
        this.matchVisualizeHandlerCallback = matchVisualizeHandlerCallback;
    }

    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    @Override
    public void onMatchEnded(String winnerName) {
        send("onMatchEnded", winnerName, null,null, null);
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

    @Override
    public void onWin(Side side, int wins) {
        send("onWin", side.toString(), null, wins, null);
    }

    @Override
    public void onReadyToServe(Side server) {
        send("onReadyToServe", server.toString(), null, null, null);
    }

    @Override
    public void onNotReadyButPlaying() {
        send("onNotReadyButPlaying", null, null, null, null);
    }

    private void send(String event, String side, Integer score, Integer wins, Side nextServer) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.WINS_PROPERTY, wins);
            json.put(JSONInfo.EVENT_PROPERTY, event);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            Payload payload = Payload.fromBytes(json.toString().getBytes(UTF_8));
            this.connection.sendPayload(this.advertiserEndpointID, payload);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE+this.advertiserEndpointID+"\n"+ex.getMessage());
        }
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
            Payload payload = Payload.fromBytes(json.toString().getBytes(UTF_8));
            this.connection.sendPayload(this.advertiserEndpointID, payload);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE+this.advertiserEndpointID+"\n"+ex.getMessage());
        }
    }

    private void handleOnRequestTableFrame() {
        Log.d("onRequestTableFrame");
        byte[] frame = this.initTrackerCallback.onCaptureFrame();

        byte[] compressedJPGBytes = CameraBytesConversions.compressCameraImageBytes(frame, this.initTrackerCallback.getCameraWidth(), this.initTrackerCallback.getCameraHeight());
        Log.d("frame length of compressed image: " + compressedJPGBytes.length + "Bytes");
        try {
            String encodedFrame = ByteToBase64.encodeToString(compressedJPGBytes);
            Log.d("encodedFrame length: " + encodedFrame.length());
            sendTableFrame(encodedFrame);
        } catch (Exception ex) {
            Log.d("UNSUPPORTED ENCODING EXCEPTION:");
            Log.d(ex.getMessage());
        }

    }

    private void sendTableFrame(String encodedFrame) {
        int numberOfPackages = (int)Math.ceil(encodedFrame.length() / (double) MAX_SIZE);
        Log.d("numberOfPackages: " + numberOfPackages);
        this.initTrackerCallback.setLoadingBarSize(numberOfPackages);
        sendTableFramePart(encodedFrame, 0, numberOfPackages, true);
    }

    private void handleOnSelectTableCorner(JSONArray tableCorners) {
        Log.d("onTableCorner: " + tableCorners);
        if (tableCorners != null) {
            int[] coordinates = new int[tableCorners.length()];

            for (int i = 0; i < tableCorners.length(); ++i) {
                coordinates[i] = tableCorners.optInt(i);
            }
            this.initTrackerCallback.setTableCorners(coordinates);
        }
    }

    private void sendTableFramePart(final String encodedFrame, final int index, final int numberOfPackages, boolean doContinue) {
        String encodedFramePart;
        if (index == numberOfPackages-1) {
            encodedFramePart = encodedFrame.substring(index*MAX_SIZE);
        } else {
            encodedFramePart = encodedFrame.substring(index*MAX_SIZE, (index+1)*MAX_SIZE);
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

    private void handleMessage(Payload payload) {
        try {
            //new String(payload.asBytes(), UTF_8)
            // Payload test = Payload.fromBytes(json.toString().getBytes(UTF_8));
            // JSONObject json2 = new JSONObject();
            // json2.put("test", new String(test.asBytes(), UTF_8))
            JSONObject json = new JSONObject(new String(payload.asBytes(), UTF_8));
            String event = json.getString(JSONInfo.EVENT_PROPERTY);
            String side;
            if(event != null) {
                switch (event) {
                    case "onPointDeduction":
                        side = json.getString(JSONInfo.SIDE_PROPERTY);
                        this.scoreManipulationCallback.onPointDeduction(Side.valueOf(side));
                        break;
                    case "onPointAddition":
                        side = json.getString(JSONInfo.SIDE_PROPERTY);
                        this.scoreManipulationCallback.onPointAddition(Side.valueOf(side));
                        break;
                    case "requestStatus":
                        if (this.callback != null) {
                            MatchStatus status = this.callback.onRequestMatchStatus();
                            sendStatusUpdate(status.getPlayerLeft(), status.getPlayerRight(),
                                    status.getScoreLeft(), status.getScoreRight(), status.getWinsLeft(),
                                    status.getWinsRight(), status.getNextServer(), status.getGamesNeededToWin());
                        }
                        break;
                    case "onPause":
                        this.scoreManipulationCallback.onPause();
                        break;
                    case "onResume":
                        this.scoreManipulationCallback.onResume();
                        break;
                    case "onRequestTableFrame":
                        handleOnRequestTableFrame();
                        break;
                    case "onSelectTableCorner":
                        JSONArray tableCorners = json.getJSONArray(JSONInfo.CORNERS);
                        handleOnSelectTableCorner(tableCorners);
                        break;
                    case "onStartMatch":
                        this.initTrackerCallback.switchToLiveActivity(Integer.parseInt(json.getString(JSONInfo.TYPE_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.SERVER_PROPERTY)));
                        break;
                    case "onRestartMatch":
                        if(this.matchVisualizeHandlerCallback != null) {
                            this.matchVisualizeHandlerCallback.restartMatch();
                        }
                        break;
                    case "onAllFramePartsReceived":
                        initTrackerCallback.frameSent();
                        break;
                    default:
                        Log.d("Invalid event received.\nevent:"+event);
                        break;
                }
            }
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to endpoint "+this.advertiserEndpointID+"\n"+ex.getMessage());
        }
    }


}
