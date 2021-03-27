package ch.m3ts.connection;

import com.pubnub.api.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.m3ts.Log;
import ch.m3ts.connection.pubnub.ByteToBase64;
import ch.m3ts.connection.pubnub.CameraBytesConversions;
import ch.m3ts.connection.pubnub.JSONInfo;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchStatus;
import ch.m3ts.tabletennis.match.MatchStatusCallback;
import ch.m3ts.tabletennis.match.game.ScoreManipulationCallback;
import ch.m3ts.tracker.init.InitTrackerCallback;
import ch.m3ts.tracker.visualization.MatchVisualizeHandlerCallback;

public abstract class ImplTrackerConnection extends Callback implements TrackerConnection {
    protected static final int MAX_SIZE = 10000;
    protected MatchStatusCallback callback;
    protected ScoreManipulationCallback scoreManipulationCallback;
    protected InitTrackerCallback initTrackerCallback;
    protected MatchVisualizeHandlerCallback matchVisualizeHandlerCallback;

    protected abstract void send(String event, String side, Integer score, Integer wins, Side nextServer);

    protected abstract void sendData(JSONObject json);

    protected abstract void sendTableFramePart(final String encodedFrame, final int index, final int numberOfPackages, boolean doContinue);

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
                        if (this.matchVisualizeHandlerCallback != null) {
                            this.matchVisualizeHandlerCallback.restartMatch();
                        }
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
}
