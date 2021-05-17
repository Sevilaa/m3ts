package ch.m3ts.connection;

import android.graphics.Point;

import com.pubnub.api.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import ch.m3ts.connection.pubnub.ByteToBase64;
import ch.m3ts.connection.pubnub.JSONInfo;
import ch.m3ts.display.DisplayConnectCallback;
import ch.m3ts.display.stats.MatchStats;
import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.EventBus;
import ch.m3ts.eventbus.Subscribable;
import ch.m3ts.eventbus.TTEvent;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.data.RequestStatsData;
import ch.m3ts.eventbus.data.RestartMatchData;
import ch.m3ts.eventbus.data.StatsData;
import ch.m3ts.eventbus.data.StatusUpdateData;
import ch.m3ts.eventbus.data.scoremanipulation.ScoreManipulationData;
import ch.m3ts.eventbus.data.todisplay.InvalidServeData;
import ch.m3ts.eventbus.data.todisplay.MatchEndedData;
import ch.m3ts.eventbus.data.todisplay.ReadyToServeData;
import ch.m3ts.eventbus.data.todisplay.ScoreData;
import ch.m3ts.eventbus.data.todisplay.ToDisplayGameWinData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.game.ScoreManipulationListener;
import ch.m3ts.util.Log;

public abstract class ImplDisplayConnection extends Callback implements ScoreManipulationListener, DisplayConnection, Subscribable {
    protected DisplayConnectCallback displayConnectCallback;
    private String encodedMultipartComplete;
    private int numberOfEncodedParts;

    protected abstract void send(String event, String side);

    protected abstract void sendData(JSONObject json);

    public void requestStatusUpdate() {
        send(ConnectionEvent.STATUS_REQUEST, null);
    }

    private void requestStats() {
        send(ConnectionEvent.STATS_REQUEST, null);
    }

    public void onRequestTableFrame() {
        send(ConnectionEvent.TABLE_FRAME_REQUEST, null);
    }

    @Override
    public void onPointDeduction(Side side) {
        send(ConnectionEvent.POINT_DEDUCTION, side.toString());
    }

    @Override
    public void onPointAddition(Side side) {
        send(ConnectionEvent.POINT_ADDITION, side.toString());
    }

    @Override
    public void onPause() {
        send(ConnectionEvent.PAUSE, null);
    }

    @Override
    public void onResume() {
        send(ConnectionEvent.RESUME, null);
    }

    private void onRestartMatch() {
        send(ConnectionEvent.MATCH_RESTART, null);
    }

    public void onSelectTableCorners(Point[] tableCorners) {
        try {
            int[] corners = new int[tableCorners.length * 2];
            for (int i = 0; i < tableCorners.length; i++) {
                Log.d("Point" + i + ": " + tableCorners[i].x + ", " + tableCorners[i].y);
                corners[2 * i] = tableCorners[i].x;
                corners[2 * i + 1] = tableCorners[i].y;
            }
            JSONObject json = new JSONObject();
            json.put(JSONInfo.CORNERS, new JSONArray(corners));
            json.put(JSONInfo.EVENT_PROPERTY, ConnectionEvent.TABLE_CORNER_SELECTION);
            sendData(json);
        } catch (JSONException ex) {
            Log.d("Unable to send JSON to endpoint \n" + ex.getMessage());
        }
    }

    public void setDisplayConnectCallback(DisplayConnectCallback displayConnectCallback) {
        this.displayConnectCallback = displayConnectCallback;
    }

    protected void handleMessage(JSONObject json) {
        try {
            String event = json.getString(JSONInfo.EVENT_PROPERTY);
            EventBus eventBus = TTEventBus.getInstance();
            if (event != null) {
                switch (event) {
                    case ConnectionEvent.MATCH_ENDED:
                        eventBus.dispatch(new TTEvent<>(new MatchEndedData(json.getString(JSONInfo.SIDE_PROPERTY))));
                        break;
                    case ConnectionEvent.SCORE:
                        eventBus.dispatch(new TTEvent<>(new ScoreData(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.SCORE_PROPERTY)),
                                Side.valueOf(json.getString(JSONInfo.NEXT_SERVER_PROPERTY)), Side.valueOf(json.getString(JSONInfo.LAST_SERVER_PROPERTY)))));
                        break;
                    case ConnectionEvent.WIN:
                        eventBus.dispatch(new TTEvent<>(new ToDisplayGameWinData(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.WINS_PROPERTY)))));
                        break;
                    case ConnectionEvent.READY_TO_SERVE:
                        eventBus.dispatch(new TTEvent<>(new ReadyToServeData(Side.valueOf(json.getString(JSONInfo.SIDE_PROPERTY)))));
                        break;
                    case ConnectionEvent.NOT_READY_BUT_PLAYING:
                        eventBus.dispatch(new TTEvent<>(new InvalidServeData()));
                        break;
                    case ConnectionEvent.STATUS_UPDATE:
                        eventBus.dispatch(new TTEvent<>(new StatusUpdateData(
                                json.getString(JSONInfo.PLAYER_NAME_LEFT_PROPERTY), json.getString(JSONInfo.PLAYER_NAME_RIGHT_PROPERTY),
                                Integer.parseInt(json.getString(JSONInfo.SCORE_LEFT_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.SCORE_RIGHT_PROPERTY)),
                                Integer.parseInt(json.getString(JSONInfo.WINS_LEFT_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.WINS_RIGHT_PROPERTY)),
                                Side.valueOf(json.getString(JSONInfo.NEXT_SERVER_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.GAMES_NEEDED_PROPERTY)))));
                        break;
                    case ConnectionEvent.CONNECTION:
                        if (displayConnectCallback != null) {
                            displayConnectCallback.onConnected();
                        }
                        break;
                    case ConnectionEvent.TABLE_FRAME:
                    case ConnectionEvent.STATS_PART:
                        this.handlePart(json);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            Log.d("Unable to receive JSON from endpoint \n" + ex.getMessage());
        }
    }

    private void handlePart(JSONObject json) throws JSONException {
        int encodedPartIndex = json.getInt(JSONInfo.PART_INDEX);
        int numberOfParts = json.getInt(JSONInfo.MULTIPART_NUMBER_OF_PARTS);
        String encodedPart = json.getString(JSONInfo.PART_DATA);
        if (encodedPartIndex == 0) {
            this.numberOfEncodedParts = 1;
            this.encodedMultipartComplete = encodedPart;
            if (json.getString(JSONInfo.EVENT_PROPERTY).equals(ConnectionEvent.TABLE_FRAME))
                this.displayConnectCallback.onImageTransmissionStarted(numberOfParts);
            if (this.numberOfEncodedParts == numberOfParts)
                handleMultipartTransmissionCompletion(json);
        } else {
            this.numberOfEncodedParts++;
            this.encodedMultipartComplete += encodedPart;
            if (json.getString(JSONInfo.EVENT_PROPERTY).equals(ConnectionEvent.TABLE_FRAME))
                this.displayConnectCallback.onImagePartReceived(encodedPartIndex + 1);
            if (encodedPartIndex == numberOfParts - 1) {
                Log.d("number of parts sent: " + numberOfParts);
                Log.d("number of parts received: " + numberOfEncodedParts);
                if (this.numberOfEncodedParts == numberOfParts) {
                    handleMultipartTransmissionCompletion(json);
                } else {
                    handleMultipartTransmissionRetry(json.getString(JSONInfo.EVENT_PROPERTY));
                }
            }
        }
    }

    private void handleMultipartTransmissionCompletion(JSONObject json) throws JSONException {
        Log.d("encodedData length: " + this.encodedMultipartComplete.length());
        switch (json.getString(JSONInfo.EVENT_PROPERTY)) {
            case ConnectionEvent.TABLE_FRAME:
                byte[] frame = ByteToBase64.decodeToByte(this.encodedMultipartComplete);
                Log.d("frame length: " + frame.length);
                this.displayConnectCallback.onImageReceived(frame, json.getInt(JSONInfo.TABLE_FRAME_WIDTH), json.getInt(JSONInfo.TABLE_FRAME_HEIGHT));
                break;
            case ConnectionEvent.STATS_PART:
                try {
                    byte[] b = android.util.Base64.decode(this.encodedMultipartComplete, android.util.Base64.DEFAULT);
                    ByteArrayInputStream bi = new ByteArrayInputStream(b);
                    ObjectInputStream si = new ObjectInputStream(bi);
                    MatchStats stats = (MatchStats) si.readObject();
                    TTEventBus.getInstance().dispatch(new TTEvent<>(new StatsData(stats)));
                } catch (Exception e) {
                    Log.d("Failed to serialize Stats:" + e.getMessage());
                }
                break;
        }
        this.numberOfEncodedParts = 0;
        this.encodedMultipartComplete = "";
    }

    private void handleMultipartTransmissionRetry(String event) {
        switch (event) {
            case ConnectionEvent.TABLE_FRAME:
                onRequestTableFrame();
                break;
            case ConnectionEvent.STATS_PART:
                requestStats();
                break;
        }
    }


    public void onStartMatch(String matchType, String server) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.TYPE_PROPERTY, matchType);
            json.put(JSONInfo.SERVER_PROPERTY, server);
            json.put(JSONInfo.EVENT_PROPERTY, ConnectionEvent.MATCH_START);
            sendData(json);
        } catch (JSONException ex) {
            Log.d("Unable to send JSON " + "\n" + ex.getMessage());
        }
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof ScoreManipulationData) {
            ScoreManipulationData manipulationData = (ScoreManipulationData) data;
            manipulationData.call(this);
        } else if (data instanceof RestartMatchData) {
            this.onRestartMatch();
        } else if (data instanceof RequestStatsData) {
            this.requestStats();
        }
    }
}
