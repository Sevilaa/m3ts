package com.android.grafika;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import cz.fmo.Lib;
import cz.fmo.R;
import cz.fmo.TrackerPubNub;
import cz.fmo.data.Track;
import cz.fmo.data.TrackSet;
import cz.fmo.events.EventDetectionCallback;
import cz.fmo.events.EventDetector;
import cz.fmo.tabletennis.GameType;
import cz.fmo.tabletennis.Match;
import cz.fmo.tabletennis.MatchType;
import cz.fmo.tabletennis.Player;
import cz.fmo.tabletennis.ScoreManipulationCallback;
import cz.fmo.tabletennis.ServeRules;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.Table;
import cz.fmo.tabletennis.UICallback;
import cz.fmo.util.Config;
import helper.OnSwipeListener;

public class DebugHandler extends android.os.Handler implements EventDetectionCallback, UICallback {
    private static final int MAX_REFRESHING_TIME_MS = 500;
    final WeakReference<DebugActivity> mActivity;
    private EventDetector eventDetector;
    private int canvasWidth;
    private int canvasHeight;
    private Paint p;
    private int videoWidth;
    private int videoHeight;
    private Config config;
    private TrackSet tracks;
    private Table table;
    private boolean hasNewTable;
    private Lib.Detection latestNearlyOutOfFrame;
    private Lib.Detection latestBounce;
    private Match match;
    private int newBounceCount;
    private ScoreManipulationCallback smc;
    private boolean useScreenForUICallback;
    private TrackerPubNub trackerPubNub;
    private UICallback uiCallback;

    public DebugHandler(@NonNull DebugActivity activity, String matchID, boolean useScreenForUICallback) {
        mActivity = new WeakReference<>(activity);
        this.useScreenForUICallback = useScreenForUICallback;
        tracks = TrackSet.getInstance();
        tracks.clear();
        hasNewTable = true;
        p = new Paint();
        uiCallback = this;
        if (!useScreenForUICallback) {
            try {
                Properties properties = new Properties();
                try (InputStream is = activity.getAssets().open("app.properties")) {
                    properties.load(is);
                    this.trackerPubNub = new TrackerPubNub(matchID, properties.getProperty("pub_key"), properties.getProperty("sub_key"));
                    uiCallback = this.trackerPubNub;
                }
            } catch (IOException ex) {
                Log.d("No properties file found, using display of this device...");
                this.useScreenForUICallback = true;
            }
        }
    }

    void initMatch(Side servingSide, MatchType matchType, Player playerLeft, Player playerRight) {
        match = new Match(matchType, GameType.G11, ServeRules.S2, playerLeft, playerRight, uiCallback, servingSide);
        if (this.trackerPubNub != null) {
            this.trackerPubNub.setTrackerPubNubCallback(match);
            this.trackerPubNub.sendStatusUpdate(playerLeft.getName(), playerRight.getName(), 0,0,0,0,servingSide);
        }
        startMatch();
        setTextInTextView(R.id.txtDebugPlayerNameLeft, playerLeft.getName());
        setTextInTextView(R.id.txtDebugPlayerNameRight, playerRight.getName());
        Timer refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new DebugHandlerRefreshTimerTask(this), new Date(), MAX_REFRESHING_TIME_MS);
    }

    @Override
    public void onBounce(Lib.Detection detection) {
        // update game logic
        // then display game state to some views
        latestBounce = detection;
        final DebugActivity activity = mActivity.get();
        final TextView mBounceCountText = activity.getmBounceCountText();
        newBounceCount = Integer.parseInt(mBounceCountText.getText().toString()) + 1;
    }

    @Override
    public void onSideChange(final Side side) {
        // use the referees current striker (might be different then side in parameter!)
        setTextInTextView(R.id.txtSide, match.getReferee().getCurrentStriker().toString());
    }

    @Override
    public void onNearlyOutOfFrame(Lib.Detection detection, Side side) {
        latestNearlyOutOfFrame = detection;
    }

    @Override
    public void onStrikeFound(Track track) {
        DebugActivity activity = mActivity.get();
        if (activity == null) {
            return;
        }
        if (activity.ismSurfaceHolderReady()) {
            SurfaceHolder surfaceHolder = activity.getmSurfaceTrack().getHolder();
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }
            if (this.canvasWidth == 0 || this.canvasHeight == 0) {
                canvasWidth = canvas.getWidth();
                canvasHeight = canvas.getHeight();
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
            setTextInTextView(R.id.txtBounce, String.valueOf(this.newBounceCount));
        }
    }

    @Override
    public void onTableSideChange(Side side) {
        // do nothing
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

    public void refreshDebugTextViews() {
        setTextInTextView(R.id.txtPlayMovieState, match.getReferee().getState().toString());
        setTextInTextView(R.id.txtPlayMovieServing, match.getReferee().getServer().toString());
        if(match.getReferee().getCurrentStriker() != null) {
            setTextInTextView(R.id.txtSide, match.getReferee().getCurrentStriker().toString());
        }
    }

    void init(Config config, int srcWidth, int srcHeight) {
        this.videoWidth = srcWidth;
        this.videoHeight = srcHeight;
        this.config = config;
        List<EventDetectionCallback> callbacks = new ArrayList<>();
        callbacks.add(this);
        callbacks.add(this.match.getReferee());
        eventDetector = new EventDetector(config, srcWidth, srcHeight, callbacks, tracks, this.table);
    }

    void startDetections() {
        Lib.detectionStart(this.videoWidth, this.videoHeight, this.config.getProcRes(), this.config.isGray(), eventDetector);
    }

    void stopDetections() {
        Lib.detectionStop();
    }

    void setTable(Table table) {
        if (table != null) {
            hasNewTable = true;
            this.table = table;
            eventDetector.setTable(table);
        }
    }

    void clearCanvas(SurfaceHolder surfaceHolder) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    void drawTable() {
        DebugActivity activity = mActivity.get();
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
            c1 = scalePoint(c1);
            c2 = scalePoint(c2);
            p.setColor(Color.CYAN);
            p.setStrokeWidth(5f);
            canvas.drawLine(c1.x, c1.y, c2.x, c2.y, p);
        }
        Point closeNetEnd = scalePoint(table.getCloseNetEnd());
        Point farNetEnd = scalePoint(table.getFarNetEnd());
        canvas.drawLine(closeNetEnd.x, closeNetEnd.y, farNetEnd.x, farNetEnd.y, p);
        surfaceHolderTable.unlockCanvasAndPost(canvas);
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
            canvas.drawCircle(scaleX(pre.centerX), scaleY(pre.centerY), pre.radius, p);
            if (pre.predecessor != null) {
                int x1 = scaleX(pre.centerX);
                int x2 = scaleX(pre.predecessor.centerX);
                int y1 = scaleY(pre.centerY);
                int y2 = scaleY(pre.predecessor.centerY);
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
            canvas.drawCircle(scaleX(latestNearlyOutOfFrame.centerX), scaleY(latestNearlyOutOfFrame.centerY), latestNearlyOutOfFrame.radius, p);
        }
    }

    private void drawLatestBounce(Canvas canvas) {
        if(latestBounce != null) {
            p.setColor(Color.rgb(255,0,0));
            p.setStrokeWidth(latestBounce.radius * 2);
            canvas.drawCircle(scaleX(latestBounce.centerX), scaleY(latestBounce.centerY), latestBounce.radius * 2, p);
        }
    }

    private int scaleY(int value) {
        float relPercentage = ((float) value) / ((float) this.videoHeight);
        return Math.round(relPercentage * this.canvasHeight);
    }

    private int scaleX(int value) {
        float relPercentage = ((float) value) / ((float) this.videoWidth);
        return Math.round(relPercentage * this.canvasWidth);
    }

    private Point scalePoint(Point p) {
        return new Point(scaleX(p.x), scaleY(p.y));
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
        final DebugActivity activity = mActivity.get();
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
