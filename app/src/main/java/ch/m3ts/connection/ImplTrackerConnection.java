package ch.m3ts.connection;

import android.util.Base64;

import com.pubnub.api.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import ch.m3ts.connection.pubnub.ByteToBase64;
import ch.m3ts.connection.pubnub.CameraBytesConversions;
import ch.m3ts.connection.pubnub.JSONInfo;
import ch.m3ts.display.statistic.StatsCreator;
import ch.m3ts.display.statistic.data.MatchData;
import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.Subscribable;
import ch.m3ts.eventbus.TTEvent;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.event.RestartMatchData;
import ch.m3ts.eventbus.event.StatusUpdateData;
import ch.m3ts.eventbus.event.scoremanipulation.PauseMatch;
import ch.m3ts.eventbus.event.scoremanipulation.PointAddition;
import ch.m3ts.eventbus.event.scoremanipulation.PointDeduction;
import ch.m3ts.eventbus.event.scoremanipulation.ResumeMatch;
import ch.m3ts.eventbus.event.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import ch.m3ts.tabletennis.match.MatchStatus;
import ch.m3ts.tabletennis.match.MatchStatusCallback;
import ch.m3ts.tracker.init.InitTrackerCallback;
import ch.m3ts.util.Log;
import ch.m3ts.util.Side;

public abstract class ImplTrackerConnection extends Callback implements TrackerConnection, DisplayUpdateListener, Subscribable {
    protected static final int MAX_SIZE = 10000;
    protected static final String ROLE = "tracker";
    private static final String JSON_SEND_EXCEPTION_MESSAGE = "Unable to send JSON to endpoint ";
    protected MatchStatusCallback callback;
    protected InitTrackerCallback initTrackerCallback;

    protected abstract void send(String event, String side, Integer score, Integer wins, Side nextServer);

    protected abstract void sendData(JSONObject json);

    protected abstract void sendPart(JSONObject json, int index, int numberOfPackages, String encodedPart);

    public void sentPart(String event, int index) {
        switch (event) {
            case ConnectionEvent.TABLE_FRAME:
                this.initTrackerCallback.updateLoadingBar(index);
                break;
            case ConnectionEvent.STATS_PART:
                // not needed so far
                break;
        }
    }

    public void sentMultipartCompletely(String event) {
        switch (event) {
            case ConnectionEvent.TABLE_FRAME:
                this.initTrackerCallback.frameSent();
                break;
            case ConnectionEvent.STATS_PART:
                // not needed so far
                break;
        }
    }

    public void setTrackerPubNubCallback(MatchStatusCallback callback) {
        this.callback = callback;
    }

    public void setInitTrackerCallback(InitTrackerCallback initTrackerCallback) {
        this.initTrackerCallback = initTrackerCallback;
    }

    protected void createPart(JSONObject header, final String encodedData, final int index, final int numberOfPackages, boolean doContinue) {
        String encodedPart;
        if (index == numberOfPackages - 1) {
            encodedPart = encodedData.substring(index * MAX_SIZE);
        } else {
            encodedPart = encodedData.substring(index * MAX_SIZE, (index + 1) * MAX_SIZE);
        }
        if (numberOfPackages == 1) doContinue = false;
        Log.d("encodedFrame part length: " + encodedPart.length());
        try {
            header.put(JSONInfo.PART_INDEX, index);
            header.put(JSONInfo.MULTIPART_NUMBER_OF_PARTS, numberOfPackages);
            header.put(JSONInfo.PART_DATA, encodedPart);
            if (doContinue) {
                sendPart(header, index, numberOfPackages, encodedData);
            } else {
                sendData(header);
            }
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + "\n" + ex.getMessage());
        }
    }

    @Override
    public void onScore(Side side, int score, Side nextServer, Side lastServer) {
        send(ConnectionEvent.SCORE, side.toString(), score, null, nextServer);
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.LAST_SERVER_PROPERTY, lastServer);
            json.put(JSONInfo.EVENT_PROPERTY, ConnectionEvent.SCORE);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            json.put(JSONInfo.ROLE_PROPERTY, ROLE);
            sendData(json);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + ex.getMessage());
        }
    }

    private void sendStats() {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            MatchData stats = StatsCreator.getInstance().createStats();
            so.writeObject(stats);
            so.flush();
            String encodedStats = android.util.Base64.encodeToString(bo.toByteArray(), Base64.DEFAULT);
            JSONObject json = new JSONObject();
            json.put(JSONInfo.EVENT_PROPERTY, ConnectionEvent.STATS_PART);
            sendMultipartData(json, encodedStats);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + ex.getMessage());
        } catch (Exception e) {
            Log.d("Failed to serialize Stats");
        }
    }

    public void onWin(Side side, int wins) {
        send(ConnectionEvent.WIN, side.toString(), null, wins, null);
    }

    public void onReadyToServe(Side server) {
        send(ConnectionEvent.READY_TO_SERVE, server.toString(), null, null, null);
    }

    public void onMatchEnded(String winnerName) {
        send(ConnectionEvent.MATCH_ENDED, winnerName, null, null, null);
    }

    public void onNotReadyButPlaying() {
        send(ConnectionEvent.NOT_READY_BUT_PLAYING, null, null, null, null);
    }

    protected void handleMessage(JSONObject json) {
        try {
            String event = json.getString(JSONInfo.EVENT_PROPERTY);
            String side;
            if (event != null) {
                switch (event) {
                    case ConnectionEvent.POINT_DEDUCTION:
                        side = json.getString(JSONInfo.SIDE_PROPERTY);
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new PointDeduction(Side.valueOf(side))));
                        break;
                    case ConnectionEvent.POINT_ADDITION:
                        side = json.getString(JSONInfo.SIDE_PROPERTY);
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new PointAddition(Side.valueOf(side))));
                        break;
                    case ConnectionEvent.STATUS_REQUEST:
                        if (this.callback != null) {
                            MatchStatus status = this.callback.onRequestMatchStatus();
                            sendStatusUpdate(status.getPlayerLeft(), status.getPlayerRight(),
                                    status.getScoreLeft(), status.getScoreRight(), status.getWinsLeft(),
                                    status.getWinsRight(), status.getNextServer(), status.getGamesNeededToWin());
                        }
                        break;
                    case ConnectionEvent.STATS_REQUEST:
                        sendStats();
                        break;
                    case ConnectionEvent.PAUSE:
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new PauseMatch()));
                        break;
                    case ConnectionEvent.RESUME:
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new ResumeMatch()));
                        break;
                    case ConnectionEvent.TABLE_FRAME_REQUEST:
                        handleOnRequestTableFrame();
                        break;
                    case ConnectionEvent.TABLE_CORNER_SELECTION:
                        JSONArray tableCorners = json.getJSONArray(JSONInfo.CORNERS);
                        handleOnSelectTableCorner(tableCorners);
                        break;
                    case ConnectionEvent.MATCH_START:
                        this.initTrackerCallback.switchToLiveActivity(Integer.parseInt(json.getString(JSONInfo.TYPE_PROPERTY)), Integer.parseInt(json.getString(JSONInfo.SERVER_PROPERTY)));
                        break;
                    case ConnectionEvent.MATCH_RESTART:
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new RestartMatchData()));
                        break;
                    default:
                        break;
                }
            }
        } catch (JSONException ex) {
            Log.d("Unable to parse JSON \n" + ex.getMessage());
        }
    }

    protected void handleOnRequestTableFrame() {
        byte[] frame = this.initTrackerCallback.onCaptureFrame();
        byte[] compressedJPGBytes = CameraBytesConversions.compressCameraImageBytes(frame, this.initTrackerCallback.getCameraWidth(), this.initTrackerCallback.getCameraHeight());
        Log.d("frame length of compressed image: " + compressedJPGBytes.length + "Bytes");
        try {
            String encodedFrame = ByteToBase64.encodeToString(compressedJPGBytes);
            Log.d("encodedFrame length: " + encodedFrame.length());
            JSONObject json = new JSONObject();
            json.put(JSONInfo.EVENT_PROPERTY, ConnectionEvent.TABLE_FRAME);
            json.put(JSONInfo.TABLE_FRAME_WIDTH, this.initTrackerCallback.getCameraWidth());
            json.put(JSONInfo.TABLE_FRAME_HEIGHT, this.initTrackerCallback.getCameraHeight());
            sendMultipartData(json, encodedFrame);
        } catch (Exception ex) {
            Log.d("UNSUPPORTED ENCODING EXCEPTION:");
            Log.d(ex.getMessage());
        }
    }

    private void sendMultipartData(JSONObject header, String encodedData) throws JSONException {
        int numberOfPackages = (int) Math.ceil(encodedData.length() / (double) MAX_SIZE);
        Log.d("numberOfPackages: " + numberOfPackages);
        if (header.getString(JSONInfo.EVENT_PROPERTY).equals(ConnectionEvent.TABLE_FRAME))
            this.initTrackerCallback.setLoadingBarSize(numberOfPackages);
        createPart(header, encodedData, 0, numberOfPackages, true);
    }

    private void handleOnSelectTableCorner(JSONArray tableCorners) {
        if (tableCorners != null) {
            int[] coordinates = new int[tableCorners.length()];
            for (int i = 0; i < tableCorners.length(); ++i) {
                coordinates[i] = tableCorners.optInt(i);
            }
            this.initTrackerCallback.setTableCorners(coordinates);
        }
    }

    public void sendStatusUpdate(String playerNameLeft, String playerNameRight, int scoreLeft,
                                 int scoreRight, int winsLeft, int winsRight, Side nextServer,
                                 int gamesNeededToWin) {
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.PLAYER_NAME_LEFT_PROPERTY, playerNameLeft);
            json.put(JSONInfo.PLAYER_NAME_RIGHT_PROPERTY, playerNameRight);
            json.put(JSONInfo.SCORE_LEFT_PROPERTY, scoreLeft);
            json.put(JSONInfo.SCORE_RIGHT_PROPERTY, scoreRight);
            json.put(JSONInfo.WINS_LEFT_PROPERTY, winsLeft);
            json.put(JSONInfo.WINS_RIGHT_PROPERTY, winsRight);
            json.put(JSONInfo.EVENT_PROPERTY, ConnectionEvent.STATUS_UPDATE);
            json.put(JSONInfo.ROLE_PROPERTY, ROLE);
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            json.put(JSONInfo.GAMES_NEEDED_PROPERTY, gamesNeededToWin);
            sendData(json);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + "\n" + ex.getMessage());
        }
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof ToDisplayData) {
            ToDisplayData displayData = (ToDisplayData) data;
            displayData.call(this);
        } else if (data instanceof StatusUpdateData) {
            StatusUpdateData updateData = (StatusUpdateData) data;
            this.sendStatusUpdate(updateData.getPlayerNameLeft(), updateData.getPlayerNameRight(),
                    updateData.getPointsLeft(), updateData.getPointsRight(), updateData.getGamesLeft(),
                    updateData.getGamesRight(), updateData.getNextServer(), updateData.getGamesNeededToWin());
        }
    }
}
