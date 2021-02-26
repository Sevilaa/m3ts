package ch.m3ts.tracker.visualization;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import ch.m3ts.Log;
import ch.m3ts.display.OnSwipeListener;
import ch.m3ts.pubnub.PubNubFactory;
import ch.m3ts.pubnub.TrackerPubNub;
import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.events.EventDetectionCallback;
import ch.m3ts.tabletennis.events.EventDetector;
import ch.m3ts.tabletennis.events.GestureCallback;
import ch.m3ts.tabletennis.events.ReadyToServeDetector;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.Match;
import ch.m3ts.tabletennis.match.MatchSettings;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tabletennis.match.Player;
import ch.m3ts.tabletennis.match.ServeRules;
import ch.m3ts.tabletennis.match.UICallback;
import ch.m3ts.tabletennis.match.game.GameType;
import ch.m3ts.tabletennis.match.game.ScoreManipulationCallback;
import ch.m3ts.tracker.ZPositionCalc;
import cz.fmo.Lib;
import cz.fmo.R;
import cz.fmo.data.Track;
import cz.fmo.data.TrackSet;
import cz.fmo.util.Config;

/**
 * Renders the images received by any video source onto the screen and also passes them to FMO.
 * Use this Handler for tasks which need to be done on Replay AND live.
 *
 * FMO then finds detections and tracks and forwards them to the EventDetector, which then calls
 * for events on this Handler.
 **/
public class MatchVisualizeHandler extends android.os.Handler implements EventDetectionCallback, UICallback, MatchVisualizeHandlerCallback, GestureCallback {
    private static final int MAX_REFRESHING_TIME_MS = 500;
    final WeakReference<MatchVisualizeActivity> mActivity;
    private EventDetector eventDetector;
    private ReadyToServeDetector serveDetector;
    private Paint p;
    private VideoScaling videoScaling;
    private Config config;
    private TrackSet tracks;
    private Table table;
    private boolean hasNewTable;
    private Lib.Detection latestNearlyOutOfFrame;
    private Lib.Detection latestBounce;
    private Match match;
    private MatchSettings matchSettings;
    private int newBounceCount;
    private ScoreManipulationCallback smc;
    private boolean useScreenForUICallback;
    private boolean waitingForGesture = false;
    private TrackerPubNub trackerPubNub;
    private UICallback uiCallback;
    private ZPositionCalc calc;
    private final double viewingAngle;

    public MatchVisualizeHandler(@NonNull MatchVisualizeActivity activity, String matchID, boolean useScreenForUICallback) {
        mActivity = new WeakReference<>(activity);
        this.useScreenForUICallback = useScreenForUICallback;
        tracks = TrackSet.getInstance();
        tracks.clear();
        hasNewTable = true;
        p = new Paint();
        uiCallback = this;
        if (!useScreenForUICallback) {
            try {
                this.trackerPubNub = PubNubFactory.createTrackerPubNub(activity.getApplicationContext(), matchID);
                uiCallback = this.trackerPubNub;
            } catch (PubNubFactory.NoPropertiesFileFoundException ex) {
                Log.d("No properties file found, using display of this device...");
                this.useScreenForUICallback = true;
            }
        }
        viewingAngle = activity.getCameraHorizontalViewAngle();
        Log.d("Camera Viewing Angle: "+activity.getCameraHorizontalViewAngle());
    }

    public void initMatch(Side servingSide, MatchType matchType, Player playerLeft, Player playerRight) {
        this.matchSettings = new MatchSettings(matchType, GameType.G11, ServeRules.S2, playerLeft, playerRight, servingSide);
        match = new Match(matchSettings, uiCallback, this);
        if (this.trackerPubNub != null) {
            this.trackerPubNub.setTrackerPubNubCallback(match);
            this.trackerPubNub.setMatchVisualizeHandlerCallback(this);
            this.trackerPubNub.sendStatusUpdate(playerLeft.getName(), playerRight.getName(), 0,0,0,0,servingSide);
        }
        startMatch();
        setTextInTextView(R.id.txtDebugPlayerNameLeft, playerLeft.getName());
        setTextInTextView(R.id.txtDebugPlayerNameRight, playerRight.getName());
        Timer refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new DebugHandlerRefreshTimerTask(this), new Date(), MAX_REFRESHING_TIME_MS);
    }

    @Override
    public void onBounce(Lib.Detection detection, Side ballBouncedOnSide) {
        // update game logic
        // then display game state to some views
        latestBounce = detection;
        final MatchVisualizeActivity activity = mActivity.get();
        final TextView mBounceCountText = activity.getmBounceCountText();
        newBounceCount = Integer.parseInt(mBounceCountText.getText().toString()) + 1;
    }

    @Override
    public void onSideChange(final Side side) {
        // use the referees current striker (might be different then side in parameter!)
        if(match.getReferee().getCurrentStriker() != null) setTextInTextView(R.id.txtSide, match.getReferee().getCurrentStriker().toString());
    }

    @Override
    public void onNearlyOutOfFrame(Lib.Detection detection, Side side) {
        latestNearlyOutOfFrame = detection;
    }

    @Override
    public void onStrikeFound(final Track track) {
        final MatchVisualizeActivity activity = mActivity.get();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity.ismSurfaceHolderReady()) {
                    SurfaceHolder surfaceHolder = activity.getmSurfaceTrack().getHolder();
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas == null) {
                        return;
                    }
                    if (videoScaling.getCanvasWidth() == 0 || videoScaling.getCanvasHeight() == 0) {
                        videoScaling.setCanvasWidth(canvas.getWidth());
                        videoScaling.setCanvasHeight(canvas.getHeight());
                    }
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    if (hasNewTable) {
                        drawTable();
                        hasNewTable = false;
                    }
                    drawTrack(canvas, track);
                    drawLatestBounce(canvas);
                    drawLatestOutOfFrameDetection(canvas);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                setTextInTextView(R.id.txtPlayMovieState, match.getReferee().getState().toString());
                setTextInTextView(R.id.txtPlayMovieServing, match.getReferee().getServer().toString());
                if(match.getReferee().getCurrentBallSide() != null) {
                    setTextInTextView(R.id.txtBounce, String.valueOf(newBounceCount));
                }
            }
        });
    }

    @Override
    public void onTableSideChange(Side side) {
        // do nothing
        setTextInTextView(R.id.txtPlayMovieServing, side.toString());
    }

    @Override
    public void onTimeout() {
        // do nothing
    }

    @Override
    public void onBallDroppedSideWays() {
        // do nothing
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onMatchEnded(String winnerName) {
        this.match = null;
        Lib.detectionStop();
        mActivity.get().getmSurfaceView().setOnTouchListener(null);
        resetScoreTextViews();
        resetGamesTextViews();
    }

    @Override
    public void onScore(Side side, int score, Side nextServer) {
        if (side == Side.LEFT) {
            setTextInTextView(R.id.txtPlayMovieScoreLeft, String.valueOf(score));
        } else {
            setTextInTextView(R.id.txtPlayMovieScoreRight, String.valueOf(score));
        }
        refreshDebugTextViews();
    }

    @Override
    public void onWin(Side side, int wins) {
        resetScoreTextViews();
        if(side == Side.LEFT) {
            setTextInTextView(R.id.txtPlayMovieGameLeft, String.valueOf(wins));
        } else {
            setTextInTextView(R.id.txtPlayMovieGameRight, String.valueOf(wins));
        }
        setCallbackForNewGame();
    }

    @Override
    public void onReadyToServe(Side server) {
        // do nothing for now
    }

    @Override
    public void restartMatch() {
        initMatch(this.matchSettings.getStartingServer(), this.matchSettings.getMatchType(), this.matchSettings.getPlayerLeft(), this.matchSettings.getPlayerRight());
        startDetections();
        refreshDebugTextViews();
    }

    public void refreshDebugTextViews() {
        setTextInTextView(R.id.txtPlayMovieState, match.getReferee().getState().toString());
        setTextInTextView(R.id.txtPlayMovieServing, match.getReferee().getServer().toString());
        if(match.getReferee().getCurrentStriker() != null) {
            setTextInTextView(R.id.txtSide, match.getReferee().getCurrentStriker().toString());
        }
    }

    public void init(Config config, int srcWidth, int srcHeight, Table table) {
        this.table = table;
        this.videoScaling = new VideoScaling(srcWidth, srcHeight);
        this.config = config;
        List<EventDetectionCallback> callbacks = new ArrayList<>();
        callbacks.add(this.match.getReferee());
        callbacks.add(this);
        this.calc = new ZPositionCalc(this.viewingAngle, table.getWidth(), this.videoScaling.getVideoWidth());
        eventDetector = new EventDetector(config, srcWidth, srcHeight, callbacks, tracks, this.table, this.calc);
    }

    public void startDetections() {
        Lib.detectionStart(this.videoScaling.getVideoWidth(), this.videoScaling.getVideoHeight(), this.config.getProcRes(), this.config.isGray(), eventDetector);
    }

    public void stopDetections() {
        Lib.detectionStop();
    }

    public void clearCanvas(SurfaceHolder surfaceHolder) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    public void drawTable() {
        MatchVisualizeActivity activity = mActivity.get();
        if (activity == null) return;
        SurfaceHolder surfaceHolderTable = activity.getmSurfaceTable().getHolder();
        Canvas canvas = surfaceHolderTable.lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Point[] corners = table.getCorners();
        for (int i = 0; i < corners.length; i++) {
            Point c1 = corners[i];
            Point c2;
            if (i < corners.length - 1) {
                c2 = corners[i + 1];
            } else {
                c2 = corners[0];
            }
            c1 = this.videoScaling.scalePoint(c1);
            c2 = this.videoScaling.scalePoint(c2);
            p.setColor(Color.CYAN);
            p.setStrokeWidth(5f);
            canvas.drawLine(c1.x, c1.y, c2.x, c2.y, p);
        }
        Point closeNetEnd = this.videoScaling.scalePoint(table.getCloseNetEnd());
        Point farNetEnd = this.videoScaling.scalePoint(table.getFarNetEnd());
        canvas.drawLine(closeNetEnd.x, closeNetEnd.y, farNetEnd.x, farNetEnd.y, p);
        surfaceHolderTable.unlockCanvasAndPost(canvas);
    }

    @Override
    public void onWaitingForGesture(Side server) {
        serveDetector = new ReadyToServeDetector(table, server, this.videoScaling.getVideoWidth(), this.videoScaling.getVideoHeight(), this.match.getReferee());
        this.waitingForGesture = true;
    }

    public boolean isWaitingForGesture() {
        return this.waitingForGesture;
    }
    public void setWaitingForGesture(boolean isWaitingForGesture) {
        this.waitingForGesture = isWaitingForGesture;
    }

    public ReadyToServeDetector getServeDetector() {
        return this.serveDetector;
    }

    private void startMatch() {
        setOnSwipeListener();
        refreshDebugTextViews();
    }

    private void drawTrack(Canvas canvas, Track t) {
        // only draw the tracks which get processed by EventDetector
        t.updateColor();
        Lib.Detection pre = t.getLatest();
        cz.fmo.util.Color.RGBA r = t.getColor();
        int c = Color.argb(255, Math.round(r.rgba[0] * 255), Math.round(r.rgba[1] * 255), Math.round(r.rgba[2] * 255));
        p.setColor(c);
        p.setStrokeWidth(pre.radius);
        int count = 0;
        while (pre != null && count < 2) {
            canvas.drawCircle(this.videoScaling.scaleX(pre.centerX), this.videoScaling.scaleY(pre.centerY), this.videoScaling.scaleY(pre.radius), p);
            if (pre.predecessor != null) {
                int x1 = this.videoScaling.scaleX(pre.centerX);
                int x2 = this.videoScaling.scaleX(pre.predecessor.centerX);
                int y1 = this.videoScaling.scaleY(pre.centerY);
                int y2 = this.videoScaling.scaleY(pre.predecessor.centerY);
                canvas.drawLine(x1, y1, x2, y2, p);
            }
            pre = pre.predecessor;
            count++;
        }
    }

    private void drawLatestOutOfFrameDetection(Canvas canvas) {
        if (latestNearlyOutOfFrame != null) {
            p.setColor(Color.rgb(255, 165, 0));
            p.setStrokeWidth(latestNearlyOutOfFrame.radius);
            canvas.drawCircle(this.videoScaling.scaleX(latestNearlyOutOfFrame.centerX), this.videoScaling.scaleY(latestNearlyOutOfFrame.centerY), latestNearlyOutOfFrame.radius, p);
        }
    }

    private void drawLatestBounce(Canvas canvas) {
        if(latestBounce != null) {
            p.setColor(Color.rgb(255,0,0));
            p.setStrokeWidth(latestBounce.radius * 2);
            canvas.drawCircle(this.videoScaling.scaleX(latestBounce.centerX), this.videoScaling.scaleY(latestBounce.centerY), latestBounce.radius * 2, p);
        }
    }

    private void resetScoreTextViews() {
        setTextInTextView(R.id.txtPlayMovieScoreLeft, String.valueOf(0));
        setTextInTextView(R.id.txtPlayMovieScoreRight, String.valueOf(0));
    }

    private void resetGamesTextViews() {
        setTextInTextView(R.id.txtPlayMovieGameLeft, String.valueOf(0));
        setTextInTextView(R.id.txtPlayMovieGameRight, String.valueOf(0));
    }

    private void setTextInTextView(int id, final String text) {
        final MatchVisualizeActivity activity = mActivity.get();
        if (activity == null) {
            return;
        }
        final TextView txtView = activity.findViewById(id);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtView.setText(text);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnSwipeListener() {
        if(match != null) {
            setCallbackForNewGame();
            mActivity.get().runOnUiThread(new Runnable() {
                public void run() {
                    mActivity.get().getmSurfaceView().setOnTouchListener(new OnSwipeListener(mActivity.get()) {
                        @Override
                        public void onSwipeDown(Side swipeSide) {
                            if (smc != null) {
                                smc.onPointDeduction(swipeSide);
                            }
                        }

                        @Override
                        public void onSwipeUp(Side swipeSide) {
                            if (smc != null) {
                                smc.onPointAddition(swipeSide);
                            }
                        }
                    });
                }
            });
        }
    }

    private void setCallbackForNewGame() {
        if(match != null) {
            if (!useScreenForUICallback) {
                this.trackerPubNub.setScoreManipulationCallback(match.getReferee());
            } else {
                this.smc = match.getReferee();
            }
        }
    }
}