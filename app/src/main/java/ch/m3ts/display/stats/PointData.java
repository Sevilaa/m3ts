package ch.m3ts.display.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.m3ts.display.stats.processing.StatsProcessing;
import ch.m3ts.tabletennis.helper.Side;

public class PointData implements Serializable {
    private final List<TrackData> tracks;
    private final String refereeDecision;
    private final Side winner;
    private final Map<Side, Integer> score;
    private final Map<Side, Integer> strikes;
    private final Side lastStriker;
    private final Side lastBallSide;
    private final Side server;
    private final int duration;
    private final boolean isCorrection;
    private Map<Side, Float> fastestStrikes;

    public PointData(String refereeDecision, List<TrackData> tracks, Side winner, int scoreLeft, int scoreRight, Side lastBallSide, Side lastStriker, Side server, int duration) {
        this.refereeDecision = refereeDecision;
        this.tracks = tracks;
        this.winner = winner;
        this.score = new HashMap<>();
        this.score.put(Side.LEFT, scoreLeft);
        this.score.put(Side.RIGHT, scoreRight);
        this.lastStriker = lastStriker;
        this.lastBallSide = lastBallSide;
        this.server = server;
        this.duration = duration;
        this.isCorrection = refereeDecision.contains("deduction");
        StatsProcessing.putTogetherTracksOfSameStrikes(tracks);
        StatsProcessing.averageZPositions(tracks);
        this.strikes = StatsProcessing.countAmountOfStrikesOfBothSides(this.tracks);
    }

    public void setFastestStrikes() {
        this.fastestStrikes = StatsProcessing.findFastestStrikeOfBothSides(tracks);
    }

    public List<TrackData> getTracks() {
        return tracks;
    }

    public String getRefereeDecision() {
        return refereeDecision;
    }

    public Side getWinner() {
        return winner;
    }

    public int getScore(Side side) {
        return score.get(side);
    }

    public Map<Side, Integer> getStrikes() {
        return strikes;
    }

    public Side getLastStriker() {
        return lastStriker;
    }

    public Side getLastBallSide() {
        return lastBallSide;
    }

    public Side getServer() {
        return server;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isCorrection() {
        return isCorrection;
    }

    public float getFastestStrike() {
        return fastestStrikes.get(Side.LEFT) > fastestStrikes.get(Side.RIGHT) ? fastestStrikes.get(Side.LEFT) : fastestStrikes.get(Side.RIGHT);
    }

    public Map<Side, Float> getFastestStrikes() {
        return fastestStrikes;
    }
}
