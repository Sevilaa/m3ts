package ch.m3ts.tracker.visualization;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.widget.TextView;

import com.google.audio.ImplAudioRecorderCallback;
import com.google.audio.core.Recorder;

import org.opencv.android.OpenCVLoader;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Timer;

import ch.m3ts.display.OnSwipeListener;
import ch.m3ts.event.Event;
import ch.m3ts.event.EventBus;
import ch.m3ts.event.Subscribable;
import ch.m3ts.event.TTEvent;
import ch.m3ts.event.TTEventBus;
import ch.m3ts.event.data.GestureData;
import ch.m3ts.event.data.eventdetector.EventDetectorEventData;
import ch.m3ts.event.data.scoremanipulation.PointAddition;
import ch.m3ts.event.data.scoremanipulation.PointDeduction;
import ch.m3ts.event.data.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.events.EventDetectionListener;
import ch.m3ts.tabletennis.events.EventDetector;
import ch.m3ts.tabletennis.events.ReadyToServeDetector;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import ch.m3ts.tabletennis.match.Match;
import ch.m3ts.tabletennis.match.MatchSettings;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tabletennis.match.Player;
import ch.m3ts.tabletennis.match.ServeRules;
import ch.m3ts.tabletennis.match.game.GameType;
import ch.m3ts.tracker.ZPositionCalc;
import cz.fmo.Lib;
import cz.fmo.R;
import cz.fmo.data.Track;
import cz.fmo.data.TrackSet;
import cz.fmo.util.Config;

/**
 * Renders the images received by any video source onto the screen and also passes them to FMO.
 * Use this Handler for tasks which need to be done on Replay AND live.
 * <p>
 * FMO then finds detections and tracks and forwards them to the EventDetector, which then calls
 * for events on this Handler.
 **/
public class MatchVisualizeHandler extends android.os.Handler implements EventDetectionListener, DisplayUpdateListener, Subscribable {
    protected static final int MAX_REFRESHING_TIME_MS = 500;
    final WeakReference<MatchVisualizeActivity> mActivity;
    private final boolean useBlackSide;
    private final boolean useAudio;
    private final TrackSet tracks;
    protected Match match;
    protected MatchSettings matchSettings;
    private EventDetector eventDetector;
    private ReadyToServeDetector serveDetector;
    private Paint p;
    private Paint oofP;
    private Paint bounceP;
    private Paint trackP;
    private VideoScaling videoScaling;
    private Config config;
    private Table table;
    private boolean hasNewTable;
    private Lib.Detection latestNearlyOutOfFrame;
    private Lib.Detection latestBounce;
    private int newBounceCount;
    private boolean waitingForGesture = false;
    private Recorder audioRecorder;

    public MatchVisualizeHandler(@NonNull MatchVisualizeActivity activity) {
        this.mActivity = new WeakReference<>(activity);
        this.tracks = TrackSet.getInstance();
        this.tracks.clear();
        this.hasNewTable = true;
        this.useBlackSide = new Config(activity.getApplicationContext()).isUseBlackSide();
        this.useAudio = new Config(activity.getApplicationContext()).isUseAudio();
        initColors(activity);
        OpenCVLoader.initDebug();
    }

    public void initMatch(Side servingSide, MatchType matchType, Player playerLeft, Player playerRight) {
        this.matchSettings = new MatchSettings(matchType, GameType.G11, ServeRules.S2, playerLeft, playerRight, servingSide);
        EventBus eventBus = TTEventBus.getInstance();
        if (this.match != null) {
            eventBus.unregister(match);
            eventBus.unregister(match.getReferee());
        }
        match = new Match(matchSettings);
        eventBus.register(match);
        startMatch();
        setTextInTextView(R.id.txtDebugPlayerNameLeft, playerLeft.getName());
        setTextInTextView(R.id.txtDebugPlayerNameRight, playerRight.getName());
        Timer refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new DebugHandlerRefreshTimerTask(this), new Date(), MAX_REFRESHING_TIME_MS);
    }

    public void deactivateReadyToServeGesture() {
        this.match.getReferee().deactivateReadyToServeGesture();
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof EventDetectorEventData) {
            EventDetectorEventData ballBounceData = (EventDetectorEventData) data;
            ballBounceData.call(this);
        } else if (data instanceof ToDisplayData) {
            ToDisplayData toDisplayData = (ToDisplayData) data;
            toDisplayData.call(this);
        } else if (data instanceof GestureData) {
            GestureData gestureData = (GestureData) data;
            this.setWaitForGesture(gestureData.getServer());
        }
    }

    @Override
    public void onBounce(Lib.Detection detection, Side ballBouncedOnSide) {
        latestBounce = detection;
        final MatchVisualizeActivity activity = mActivity.get();
        final TextView mBounceCountText = activity.getmBounceCountText();
        newBounceCount = Integer.parseInt(mBounceCountText.getText().toString()) + 1;
    }

    @Override
    public void onSideChange(final Side side) {
        // use the referees current striker (might be different then side in parameter!)
        if (match.getReferee().getCurrentStriker() != null)
            setTextInTextView(R.id.txtSide, match.getReferee().getCurrentStriker().toString());
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
                    initVideoScaling(canvas);
                    drawDebugInfo(canvas, track);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                setTextInTextView(R.id.txtPlayMovieState, match.getReferee().getState().toString());
                setTextInTextView(R.id.txtPlayMovieServing, match.getReferee().getServer().toString());
                if (match.getReferee().getCurrentBallSide() != null) {
                    setTextInTextView(R.id.txtBounce, String.valueOf(newBounceCount));
                }
            }
        });
    }

    void drawDebugInfo(Canvas canvas, Track track) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (hasNewTable) {
            drawTable();
            hasNewTable = false;
        }
        drawTrack(canvas, track);
        drawLatestBounce(canvas);
        drawLatestOutOfFrameDetection(canvas);
    }

    void initVideoScaling(Canvas canvas) {
        if (videoScaling.getCanvasWidth() == 0 || videoScaling.getCanvasHeight() == 0) {
            videoScaling.setCanvasWidth(canvas.getWidth());
            videoScaling.setCanvasHeight(canvas.getHeight());
        }
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
    public void onAudioBounce(Side side) {
        // do nothing for now
    }

    @Override
    public void onBallDroppedSideWays() {
        // do nothing
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onMatchEnded(String winnerName) {
        this.stopDetections();
        mActivity.get().getmSurfaceView().setOnTouchListener(null);
        resetScoreTextViews();
        resetGamesTextViews();
    }

    @Override
    public void onScore(Side side, int score, Side nextServer, Side lastServer) {
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
        if (side == Side.LEFT) {
            setTextInTextView(R.id.txtPlayMovieGameLeft, String.valueOf(wins));
        } else {
            setTextInTextView(R.id.txtPlayMovieGameRight, String.valueOf(wins));
        }
    }

    @Override
    public void onReadyToServe(Side server) {
        // do nothing for now
    }

    @Override
    public void onNotReadyButPlaying() {
        // do nothing for now
    }

    public void refreshDebugTextViews() {
        setTextInTextView(R.id.txtPlayMovieState, match.getReferee().getState().toString());
        setTextInTextView(R.id.txtPlayMovieServing, match.getReferee().getServer().toString());
        if (match.getReferee().getCurrentStriker() != null) {
            setTextInTextView(R.id.txtSide, match.getReferee().getCurrentStriker().toString());
        }
    }

    public void init(Config config, int srcWidth, int srcHeight, Table table, double viewingAngle) {
        this.table = table;
        this.videoScaling = new VideoScaling(srcWidth, srcHeight);
        this.config = config;
        ZPositionCalc calc = new ZPositionCalc(viewingAngle, table.getWidth(), srcWidth);
        this.eventDetector = new EventDetector(config, srcWidth, srcHeight, tracks, this.table, calc);
        if (useAudio)
            this.audioRecorder = new Recorder(new ImplAudioRecorderCallback(this.eventDetector));
        this.match.getReferee().initState();
    }

    /**
     * open connections / subscriptions when parent activity is pausing (onPause, Pause Button, etc.)
     */
    public void onResumeActivity() {
        TTEventBus eventBus = TTEventBus.getInstance();
        eventBus.register(this);
        if (this.match != null) {
            eventBus.register(match);
            eventBus.register(match.getReferee());
        }
    }

    /**
     * Close open connections / subscriptions when parent activity is pausing (onPause, Pause Button, etc.)
     */
    public void onPauseActivity() {
        TTEventBus eventBus = TTEventBus.getInstance();
        eventBus.unregister(this);
        if (this.match != null) {
            eventBus.unregister(match);
            eventBus.unregister(match.getReferee());
        }
    }

    public void startDetections() {
        Lib.detectionStart(this.videoScaling.getVideoWidth(), this.videoScaling.getVideoHeight(), this.config.getProcRes(), this.config.isGray(), eventDetector);
        if (this.audioRecorder != null) this.audioRecorder.start();
    }

    public void stopDetections() {
        Lib.detectionStop();
        if (this.audioRecorder != null) this.audioRecorder.stop();
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
            canvas.drawLine(c1.x, c1.y, c2.x, c2.y, p);
        }
        Point closeNetEnd = this.videoScaling.scalePoint(table.getCloseNetEnd());
        canvas.drawCircle(closeNetEnd.x, closeNetEnd.y, 10f, p);
        canvas.drawLine(
                closeNetEnd.x,
                closeNetEnd.y,
                closeNetEnd.x,
                Math.round(closeNetEnd.y - 0.06 * this.videoScaling.scaleX(this.table.getWidth())), // 0.06 is relative length of a table tennis net to the table width
                p);
        surfaceHolderTable.unlockCanvasAndPost(canvas);
    }

    private void setWaitForGesture(Side server) {
        if (this.match != null) {
            serveDetector = new ReadyToServeDetector(table, server, this.match.getReferee(), this.useBlackSide);
            this.waitingForGesture = true;
        }
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

    public int getVideoWidth() {
        return this.videoScaling.getVideoWidth();
    }

    public int getVideoHeight() {
        return this.videoScaling.getVideoHeight();
    }

    private void initColors(Activity activity) {
        this.p = new Paint();
        this.p.setColor(activity.getColor(R.color.accent_color));
        this.p.setStrokeWidth(5f);
        this.oofP = new Paint();
        oofP.setColor(Color.rgb(255, 165, 0));
        this.bounceP = new Paint();
        bounceP.setColor(Color.RED);
        this.trackP = new Paint();
    }

    protected void startMatch() {
        setOnSwipeListener();
        refreshDebugTextViews();
    }

    private void drawTrack(Canvas canvas, Track t) {
        // only draw the tracks which get processed by EventDetector
        t.updateColor();
        Lib.Detection pre = t.getLatest();
        cz.fmo.util.Color.RGBA r = t.getColor();
        int c = Color.argb(255, Math.round(r.rgba[0] * 255), Math.round(r.rgba[1] * 255), Math.round(r.rgba[2] * 255));
        trackP.setColor(c);
        trackP.setStrokeWidth(pre.radius);
        int count = 0;
        while (pre != null && count < 2) {
            canvas.drawCircle(this.videoScaling.scaleX(pre.centerX), this.videoScaling.scaleY(pre.centerY), this.videoScaling.scaleY(pre.radius), trackP);
            if (pre.predecessor != null) {
                int x1 = this.videoScaling.scaleX(pre.centerX);
                int x2 = this.videoScaling.scaleX(pre.predecessor.centerX);
                int y1 = this.videoScaling.scaleY(pre.centerY);
                int y2 = this.videoScaling.scaleY(pre.predecessor.centerY);
                canvas.drawLine(x1, y1, x2, y2, trackP);
            }
            pre = pre.predecessor;
            count++;
        }
    }

    private void drawLatestOutOfFrameDetection(Canvas canvas) {
        if (latestNearlyOutOfFrame != null) {
            oofP.setStrokeWidth(latestNearlyOutOfFrame.radius);
            canvas.drawCircle(this.videoScaling.scaleX(latestNearlyOutOfFrame.centerX), this.videoScaling.scaleY(latestNearlyOutOfFrame.centerY), latestNearlyOutOfFrame.radius, oofP);
        }
    }

    private void drawLatestBounce(Canvas canvas) {
        if (latestBounce != null) {
            bounceP.setStrokeWidth(latestBounce.radius * 2);
            canvas.drawCircle(this.videoScaling.scaleX(latestBounce.centerX), this.videoScaling.scaleY(latestBounce.centerY), latestBounce.radius * 2, bounceP);
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

    protected void setTextInTextView(int id, final String text) {
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
        if (match != null) {
            mActivity.get().runOnUiThread(new Runnable() {
                public void run() {
                    mActivity.get().getmSurfaceView().setOnTouchListener(new OnSwipeListener(mActivity.get()) {
                        @Override
                        public void onSwipeDown(Side swipeSide) {
                            TTEventBus.getInstance().dispatch(new TTEvent<>(new PointDeduction(swipeSide)));
                        }

                        @Override
                        public void onSwipeUp(Side swipeSide) {
                            TTEventBus.getInstance().dispatch(new TTEvent<>(new PointAddition(swipeSide)));
                        }
                    });
                }
            });
        }
    }
}