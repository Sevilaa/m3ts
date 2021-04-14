package ch.m3ts.connection;

import com.pubnub.api.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.m3ts.connection.pubnub.ByteToBase64;
import ch.m3ts.connection.pubnub.CameraBytesConversions;
import ch.m3ts.connection.pubnub.JSONInfo;
import ch.m3ts.event.Event;
import ch.m3ts.event.Subscribable;
import ch.m3ts.event.TTEvent;
import ch.m3ts.event.TTEventBus;
import ch.m3ts.event.data.RestartMatchData;
import ch.m3ts.event.data.scoremanipulation.PauseMatch;
import ch.m3ts.event.data.scoremanipulation.PointAddition;
import ch.m3ts.event.data.scoremanipulation.PointDeduction;
import ch.m3ts.event.data.scoremanipulation.ResumeMatch;
import ch.m3ts.event.data.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import ch.m3ts.tabletennis.match.MatchStatus;
import ch.m3ts.tabletennis.match.MatchStatusCallback;
import ch.m3ts.tracker.init.InitTrackerCallback;
import ch.m3ts.util.Log;

public abstract class ImplTrackerConnection extends Callback implements TrackerConnection, DisplayUpdateListener, Subscribable {
    protected static final int MAX_SIZE = 10000;
    protected static final String ROLE = "tracker";
    private static final String JSON_SEND_EXCEPTION_MESSAGE = "Unable to send JSON to endpoint ";
    protected MatchStatusCallback callback;
    protected InitTrackerCallback initTrackerCallback;
    protected abstract void send(String event, String side, Integer score, Integer wins, Side nextServer);

    protected abstract void sendData(JSONObject json);

    protected abstract void sendTableFramePart(final String encodedFrame, final int index, final int numberOfPackages, boolean doContinue);

    public void setTrackerPubNubCallback(MatchStatusCallback callback) {
        this.callback = callback;
    }

    public void setInitTrackerCallback(InitTrackerCallback initTrackerCallback) {
        this.initTrackerCallback = initTrackerCallback;
    }

    @Override
    public void onScore(Side side, int score, Side nextServer, Side lastServer) {
        send("onScore", side.toString(), score, null, nextServer);
        try {
            JSONObject json = new JSONObject();
            json.put(JSONInfo.SIDE_PROPERTY, side);
            json.put(JSONInfo.SCORE_PROPERTY, score);
            json.put(JSONInfo.LAST_SERVER_PROPERTY, lastServer);
            json.put(JSONInfo.EVENT_PROPERTY, "onScore");
            json.put(JSONInfo.NEXT_SERVER_PROPERTY, nextServer);
            json.put(JSONInfo.ROLE_PROPERTY, ROLE);
            sendData(json);
        } catch (JSONException ex) {
            Log.d(JSON_SEND_EXCEPTION_MESSAGE + ex.getMessage());
        }
    }

    public void onWin(Side side, int wins) {
        send("onWin", side.toString(), null, wins, null);
    }

    public void onReadyToServe(Side server) {
        send("onReadyToServe", server.toString(), null, null, null);
    }

    public void onMatchEnded(String winnerName) {
        send("onMatchEnded", winnerName, null, null, null);
    }

    public void onNotReadyButPlaying() {
        send("onNotReadyButPlaying", null, null, null, null);
    }

    protected void handleMessage(JSONObject json) {
        try {
            String event = json.getString(JSONInfo.EVENT_PROPERTY);
            String side;
            if (event != null) {
                switch (event) {
                    case "onPointDeduction":
                        side = json.getString(JSONInfo.SIDE_PROPERTY);
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new PointDeduction(Side.valueOf(side))));
                        break;
                    case "onPointAddition":
                        side = json.getString(JSONInfo.SIDE_PROPERTY);
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new PointAddition(Side.valueOf(side))));
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
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new PauseMatch()));
                        break;
                    case "onResume":
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new ResumeMatch()));
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
                        TTEventBus.getInstance().dispatch(new TTEvent<>(new RestartMatchData()));
                        break;
                    default:
                        Log.d("Invalid event received.\nevent:" + event);
                        break;
                }
            }
        } catch (JSONException ex) {
            Log.d("Unable to parse JSON \n" + ex.getMessage());
        }
    }

    protected void handleOnRequestTableFrame() {
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
        int numberOfPackages = (int) Math.ceil(encodedFrame.length() / (double) MAX_SIZE);
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
            json.put(JSONInfo.EVENT_PROPERTY, "onStatusUpdate");
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
        }
    }
}
