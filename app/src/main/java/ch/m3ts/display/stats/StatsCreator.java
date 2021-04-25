package ch.m3ts.display.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.m3ts.tabletennis.helper.Side;
import cz.fmo.Lib;
import cz.fmo.data.Track;

public class StatsCreator {
    private static StatsCreator instance;
    private List<PointData> points = new ArrayList<>();
    private List<GameStats> games = new ArrayList<>();
    private String formattedMatchStart;
    private Map<Side, String> playerNames = new HashMap<>();
    private Map<Side, Integer> strikes = new HashMap<>();

    private StatsCreator() {
    }

    public static StatsCreator getInstance() {
        if (StatsCreator.instance == null) {
            StatsCreator.instance = new StatsCreator();
        }
        return StatsCreator.instance;
    }

    public void addPoint(String decision, Side winner, int scoreLeft, int scoreRight, int strikes, Side ballSide, Side striker, Side server, int duration, List<Track> tracks) {
        List<DetectionData> detections = new ArrayList<>();
        List<TrackData> trackData = new ArrayList<>();
        for (Track track : tracks) {
            Lib.Detection latest = track.getLatest();
            while (latest != null) {
                detections.add(new DetectionData(latest.centerX, latest.centerY, (int) latest.centerZ, latest.velocity, latest.isBounce));
                latest = latest.predecessor;
            }
            trackData.add(new TrackData(detections, track.getAvgVelocity(), track.getStriker()));
            detections.clear();
        }
        if (this.strikes.get(Side.LEFT) == null) this.strikes.put(Side.LEFT, 0);
        if (this.strikes.get(Side.RIGHT) == null) this.strikes.put(Side.RIGHT, 0);
        PointData point = new PointData(decision, trackData, winner, scoreLeft, scoreRight, ballSide, striker, server, duration, this.strikes);
        if (points.isEmpty() && !games.isEmpty() && scoreLeft + scoreRight > 1) {
            GameStats lastGame = games.get(games.size() - 1);
            lastGame.getPoints().add(point);
            games.set(games.size() - 1, new GameStats(lastGame.getPoints()));
        } else points.add(point);
        this.strikes = new HashMap<>();
    }

    public void addStrike(Side side) {
        Integer strikes = this.strikes.get(side);
        if (strikes == null) strikes = 0;
        this.strikes.put(side, strikes + 1);
    }

    public void addMetaData(String start, String playerLeft, String playerRight) {
        this.games = new ArrayList<>();
        this.points = new ArrayList<>();
        this.playerNames = new HashMap<>();
        this.formattedMatchStart = start;
        this.playerNames.put(Side.LEFT, playerLeft);
        this.playerNames.put(Side.RIGHT, playerRight);
    }

    public void addGame() {
        if (!this.points.isEmpty()) {
            GameStats stats = new GameStats(this.points);
            this.games.add(stats);
            this.points = new ArrayList<>();
        }
    }

    public void resetGame() {
        if (this.games.isEmpty()) return;
        List<PointData> lastGamePoints = this.games.get(this.games.size() - 1).getPoints();
        lastGamePoints.addAll(points);
        this.points = lastGamePoints;
        this.games.remove(this.games.size() - 1);
    }

    public MatchStats createStats() {
        return new MatchStats(games, playerNames.get(Side.LEFT), playerNames.get(Side.RIGHT), formattedMatchStart);
    }
}
