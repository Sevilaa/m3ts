package ch.m3ts.tracker.visualization.replay.benchmark;

import android.support.annotation.NonNull;

import java.util.Locale;

import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.EventBus;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.event.ball.BallTrackData;
import ch.m3ts.eventbus.event.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.match.Match;
import ch.m3ts.tabletennis.match.MatchSettings;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tabletennis.match.Player;
import ch.m3ts.tabletennis.match.ServeRules;
import ch.m3ts.tabletennis.match.game.GameType;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import ch.m3ts.tracker.visualization.replay.ReplayDetectionCallback;
import ch.m3ts.tracker.visualization.replay.ReplayHandler;
import ch.m3ts.util.Log;
import ch.m3ts.util.Side;
import ch.m3ts.util.Table;
import cz.fmo.data.Track;
import cz.fmo.util.Config;

/**
 * Renders the images received by a mp4 file (see ./lib/VideoPlayer) onto the screen and also passes them to FMO.
 * <p>
 * FMO then finds detections and tracks and forwards them to the EventDetector, which then calls
 * for events on this Handler.
 **/
public class BenchmarkHandler extends ReplayHandler implements ReplayDetectionCallback {
    private static final String WRONG_JUDGEMENT_LOG_TEXT = "Wrong judgement on clip %s";
    private static final String NO_JUDGEMENT_LOG_TEXT = "No judgement on clip %s";
    private static final String ON_SCORE_LOG_TEXT = "onScore: %d Side: %s";
    private Side whoShouldScore;
    private int correctJudgementCalls;
    private int countOnScoreEventsPerClip;
    private String clipId;

    public BenchmarkHandler(@NonNull MatchVisualizeActivity activity) {
        super(activity);
    }

    public int getCorrectJudgementCalls() {
        int calls = correctJudgementCalls;
        this.correctJudgementCalls = 0;
        return calls;
    }

    public void setClipId(String clipId) {
        this.clipId = clipId;
    }

    /**
     * Gets called when a new clip is loaded, sets the "truth" on which side should win.
     *
     * @param whoShouldScore Side which should score on the next onScore event.
     */
    public void setWhoShouldScore(Side whoShouldScore) {
        this.whoShouldScore = whoShouldScore;
    }

    public void initBenchmarkMatch(Side servingSide) {
        initMatch(servingSide, MatchType.BO5, new Player("Hans"), new Player("Peter"));
    }

    @Override
    public void init(Config config, int srcWidth, int srcHeight, Table table, double viewingAngle) {
        super.init(config, srcWidth, srcHeight, table, viewingAngle);
        this.match.getReferee().onGestureDetected();
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
            ((BallTrackData) data).call(this);
        }
    }

    @Override
    protected void updateTextViews(Track track) {
        // do nothing
    }


    public void onClipEnded() {
        if (this.countOnScoreEventsPerClip == 0) {
            this.countOnScoreEventsPerClip = 1;
            match.getReferee().onPointAddition(whoShouldScore);
            Log.d(String.format(NO_JUDGEMENT_LOG_TEXT, clipId));
        }
        match.getReferee().onGestureDetected();
        this.countOnScoreEventsPerClip = 0;
    }

    @Override
    public void onScore(Side scorer, int score, Side nextServer, Side lastServer) {
        Log.d(String.format(Locale.US, ON_SCORE_LOG_TEXT, score, scorer.toString()));
        this.countOnScoreEventsPerClip++;
        if (this.countOnScoreEventsPerClip == 1) {
            if (scorer == whoShouldScore) {
                this.correctJudgementCalls++;
            } else {
                Log.d(String.format(WRONG_JUDGEMENT_LOG_TEXT, clipId));
                // correct the score - for some reason this will screw up the log from referee
                // so if you want to see judgement message, comment/remove the 2 lines below
                match.getReferee().onPointDeduction(scorer);
                match.getReferee().onPointAddition(Side.getOpposite(scorer));
            }
        }
    }

    @Override
    public void onWin(Side side, int wins) {
        // do nothing
    }
}
