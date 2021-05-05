package ch.m3ts.tracker.visualization.replay.benchmark;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;

import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.EventBus;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.data.eventdetector.BallTrackData;
import ch.m3ts.eventbus.data.eventdetector.EventDetectorEventData;
import ch.m3ts.eventbus.data.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.Match;
import ch.m3ts.tabletennis.match.MatchSettings;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tabletennis.match.Player;
import ch.m3ts.tabletennis.match.ServeRules;
import ch.m3ts.tabletennis.match.game.GameType;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import ch.m3ts.tracker.visualization.MatchVisualizeHandler;
import ch.m3ts.tracker.visualization.replay.ReplayDetectionCallback;
import ch.m3ts.util.Log;
import cz.fmo.Lib;
import cz.fmo.data.Track;

/**
 * Renders the images received by a mp4 file (see ./lib/VideoPlayer) onto the screen and also passes them to FMO.
 * <p>
 * FMO then finds detections and tracks and forwards them to the EventDetector, which then calls
 * for events on this Handler.
 **/
public class BenchmarkHandler extends MatchVisualizeHandler implements ReplayDetectionCallback {
    public BenchmarkHandler(@NonNull MatchVisualizeActivity activity) {
        super(activity);
    }

    public void initBenchmarkMatch(Side servingSide) {
        initMatch(servingSide, MatchType.BO5, new Player("Hans"), new Player("Peter"));
        super.deactivateReadyToServeGesture();
    }

    @Override
    public void onEncodedFrame(byte[] dataYUV420SP) {
        try {
            Lib.detectionFrame(dataYUV420SP);
        } catch (Exception ex) {
            Log.e(ex.getMessage(), ex);
        }
    }

    @Override
    public void refreshDebugTextViews() {
        // do nothing
    }

    @Override
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
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof ToDisplayData) {
            ToDisplayData toDisplayData = (ToDisplayData) data;
            toDisplayData.call(this);
        } else if (data instanceof BallTrackData) {
            EventDetectorEventData eventDetectorEventData = (EventDetectorEventData) data;
            eventDetectorEventData.call(this);
        }
    }

    @Override
    public void onStrikeFound(final Track t) {
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
                    // only draw the tracks which get processed by EventDetector
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    t.updateColor();
                    Lib.Detection pre = t.getLatest();
                    cz.fmo.util.Color.RGBA r = t.getColor();
                    int c = Color.argb(255, Math.round(r.rgba[0] * 255), Math.round(r.rgba[1] * 255), Math.round(r.rgba[2] * 255));
                    Paint trackPaint = new Paint();
                    trackPaint.setColor(c);
                    trackPaint.setStrokeWidth(pre.radius);
                    int count = 0;
                    while (pre != null && count < 2) {
                        canvas.drawCircle(videoScaling.scaleX(pre.centerX), videoScaling.scaleY(pre.centerY), videoScaling.scaleY(pre.radius), trackPaint);
                        if (pre.predecessor != null) {
                            int x1 = videoScaling.scaleX(pre.centerX);
                            int x2 = videoScaling.scaleX(pre.predecessor.centerX);
                            int y1 = videoScaling.scaleY(pre.centerY);
                            int y2 = videoScaling.scaleY(pre.predecessor.centerY);
                            canvas.drawLine(x1, y1, x2, y2, trackPaint);
                        }
                        pre = pre.predecessor;
                        count++;
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        });
    }

    @Override
    public void onMatchEnded(String winnerName) {

    }

    @Override
    public void onScore(Side scorer, int score, Side nextServer, Side lastServer) {
        Log.d("JOE BIDEN => Score: " + score + " Side: " + scorer.toString());
    }

    @Override
    public void onWin(Side winner, int wins) {

    }

    @Override
    public void onReadyToServe(Side server) {

    }

    @Override
    public void onNotReadyButPlaying() {

    }
}
