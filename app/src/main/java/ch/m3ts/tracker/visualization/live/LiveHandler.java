package ch.m3ts.tracker.visualization.live;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.nearby.connection.PayloadCallback;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Timer;

import ch.m3ts.connection.ConnectionCallback;
import ch.m3ts.connection.ConnectionHelper;
import ch.m3ts.connection.ImplTrackerConnection;
import ch.m3ts.connection.NearbyTrackerConnection;
import ch.m3ts.connection.TrackerConnection;
import ch.m3ts.connection.pubnub.PubNubFactory;
import ch.m3ts.event.Event;
import ch.m3ts.event.TTEventBus;
import ch.m3ts.event.data.RestartMatchData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.Match;
import ch.m3ts.tabletennis.match.MatchSettings;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tabletennis.match.Player;
import ch.m3ts.tabletennis.match.ServeRules;
import ch.m3ts.tabletennis.match.game.GameType;
import ch.m3ts.tracker.visualization.DebugHandlerRefreshTimerTask;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import ch.m3ts.tracker.visualization.MatchVisualizeHandler;
import ch.m3ts.util.Log;
import ch.m3ts.util.OpenCVHelper;
import cz.fmo.Lib;
import cz.fmo.R;
import cz.fmo.camera.CameraThread;
import cz.fmo.data.Track;
import cz.fmo.util.Config;

/**
 * Renders the images received by the camera API onto the screen and also passes them to FMO.
 * <p>
 * FMO then finds detections and tracks and forwards them to the EventDetector, which then calls
 * for events on this Handler.
 **/
public class LiveHandler extends MatchVisualizeHandler implements CameraThread.Callback, ConnectionCallback {
    private static final int CAMERA_ERROR = 2;
    private final boolean doDrawDebugInfo;
    private final WeakReference<LiveActivity> mLiveActivity;
    private TrackerConnection connection;

    public LiveHandler(@NonNull MatchVisualizeActivity activity, String matchID) {
        super(activity);
        this.doDrawDebugInfo = new Config(activity).isUseDebug();
        this.mLiveActivity = new WeakReference<>((LiveActivity) activity);
        TextView displayConnectedText = (TextView) activity.findViewById(R.id.display_connected_status);
        try {
            if (new Config(mLiveActivity.get()).isUsingPubnub()) {
                this.connection = PubNubFactory.createTrackerPubNub(activity.getApplicationContext(), matchID);
            } else {
                this.connection = NearbyTrackerConnection.getInstance();
                ((NearbyTrackerConnection) this.connection).setConnectionCallback(this);
            }
            TTEventBus.getInstance().register((ImplTrackerConnection) this.connection);
        } catch (PubNubFactory.NoPropertiesFileFoundException ex) {
            Log.d("No properties file found, using display of this device...");
            displayConnectedText.setText(activity.getString(R.string.trackerStatusConnectedWithDisplayFail));
            displayConnectedText.setTextColor(activity.getColor(R.color.color_error));
        }
        if (!doDrawDebugInfo) {
            activity.findViewById(R.id.playMovie_debugGrid).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.debugScoreLayoutWrapper).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResumeActivity() {
        TTEventBus.getInstance().register((ImplTrackerConnection) this.connection);
        super.onResumeActivity();
    }

    @Override
    public void onPauseActivity() {
        TTEventBus.getInstance().unregister((ImplTrackerConnection) this.connection);
        super.onPauseActivity();
    }

    @Override
    public void onCameraRender() {
        LiveActivity liveActivity = mLiveActivity.get();
        if (liveActivity == null) return;
        if (liveActivity.getmEncode() == null) return;
        liveActivity.getmEncode().getHandler().sendFlush();
    }

    @Override
    public void onCameraFrame(byte[] dataYUV420SP) {
        Lib.detectionFrame(dataYUV420SP);
        if(isWaitingForGesture()) {
            setWaitingForGesture(!getServeDetector().isReadyToServe(OpenCVHelper.convertYUVBytesToBGRMat(dataYUV420SP, getVideoWidth(), getVideoHeight())));
        }
    }

    @Override
    public void onCameraError() {
        if (hasMessages(CAMERA_ERROR)) return;
        sendMessage(obtainMessage(CAMERA_ERROR));
    }

    @Override
    public void initMatch(Side servingSide, MatchType matchType, Player playerLeft, Player playerRight) {
        this.matchSettings = new MatchSettings(matchType, GameType.G11, ServeRules.S2, playerLeft, playerRight, servingSide);
        this.match = new Match(matchSettings);
        if (mLiveActivity.get() != null)
            this.match.getReferee().debugToFile(mLiveActivity.get().getApplicationContext());
        this.connection.setTrackerPubNubCallback(match);
        this.connection.sendStatusUpdate(playerLeft.getName(), playerRight.getName(), 0,0,0,0,servingSide, matchType.gamesNeededToWin);
        startMatch();
        if(doDrawDebugInfo) {
            setTextInTextView(R.id.txtDebugPlayerNameLeft, playerLeft.getName());
            setTextInTextView(R.id.txtDebugPlayerNameRight, playerRight.getName());
            Timer refreshTimer = new Timer();
            refreshTimer.scheduleAtFixedRate(new DebugHandlerRefreshTimerTask(this), new Date(), MAX_REFRESHING_TIME_MS);
        }
    }

    @Override
    public void onStrikeFound(Track track) {
        if(doDrawDebugInfo) {
            super.onStrikeFound(track);
        }
    }

    @Override
    public void onSideChange(final Side side) {
        // use the referees current striker (might be different then side in parameter!)
        if(doDrawDebugInfo) {
            super.onSideChange(side);
        }
    }

    private void restartMatch() {
        this.match.restartMatch();
        this.startDetections();
        this.connection.sendStatusUpdate(this.matchSettings.getPlayerLeft().getName(), this.matchSettings.getPlayerRight().getName(), 0, 0, 0, 0, this.matchSettings.getStartingServer(), this.matchSettings.getMatchType().gamesNeededToWin);
        refreshDebugTextViews();
    }

    @Override
    public void onDiscoverFailure() {
        // not needed
    }

    @Override
    public void onRejection() {
        // not needed
    }

    @Override
    public void onConnection(String endpoint) {
        // not needed
    }

    @Override
    public void onConnecting(String endpointId, String endpointName, String token, PayloadCallback callback) {
        // not needed
    }

    @Override
    public void onDisconnection(String endpoint) {
        ConnectionHelper.makeDisconnectDialog(mLiveActivity.get());
    }

    public void setConnectCallback(ConnectionCallback callback) {
        if (this.connection instanceof NearbyTrackerConnection) {
            ((NearbyTrackerConnection) this.connection).setConnectionCallback(callback);
        }
    }

    @Override
    public void handle(Event<?> event) {
        super.handle(event);
        Object data = event.getData();
        if (data instanceof RestartMatchData) {
            this.restartMatch();
        }
    }
}
