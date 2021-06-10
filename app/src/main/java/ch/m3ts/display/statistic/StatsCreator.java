package ch.m3ts.display.statistic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.m3ts.detection.ZPositionCalc;
import ch.m3ts.display.statistic.data.DetectionData;
import ch.m3ts.display.statistic.data.GameData;
import ch.m3ts.display.statistic.data.MatchData;
import ch.m3ts.display.statistic.data.PointData;
import ch.m3ts.display.statistic.data.TrackData;
import ch.m3ts.display.statistic.processing.StatsProcessing;
import ch.m3ts.util.Side;
import cz.fmo.Lib;
import cz.fmo.data.Track;

public class StatsCreator {
    private final Map<Side, Integer> tableCorners = new HashMap<>();
    private static StatsCreator instance;
    private List<PointData> points = new ArrayList<>();
    private List<GameData> games = new ArrayList<>();
    private String formattedMatchStart;
    private Map<Side, String> playerNames = new HashMap<>();
    private ZPositionCalc zCalc;

    private StatsCreator() {
    }

    public static StatsCreator getInstance() {
        if (StatsCreator.instance == null) {
            StatsCreator.instance = new StatsCreator();
        }
        return StatsCreator.instance;
    }

    public void setZCalc(ZPositionCalc zCalc) {
        this.zCalc = zCalc;
    }

    public void addTableCorners(int tableCornerLeft, int tableCornerRight) {
        this.tableCorners.put(Side.LEFT, tableCornerLeft);
        this.tableCorners.put(Side.RIGHT, tableCornerRight);
    }

    public void addPoint(String decision, Side winner, int scoreLeft, int scoreRight, Side ballSide, Side striker, Side server, int duration, List<Track> tracks) {
        List<DetectionData> detections = new ArrayList<>();
        List<TrackData> trackDataList = new ArrayList<>();
        for (Track track : tracks) {
            Lib.Detection latest = track.getLatest();
            while (latest != null) {
                detections.add(new DetectionData(latest.centerX, latest.centerY, latest.centerZ, latest.velocity, latest.isBounce, (int) latest.directionX));
                latest = latest.predecessor;
            }
            trackDataList.add(new TrackData(detections, track.getAvgVelocity(), track.getStriker()));
            detections = new ArrayList<>();
        }
        PointData point = new PointData(decision, trackDataList, winner, scoreLeft, scoreRight, ballSide, striker, server, duration);
        StatsProcessing.calculatePositionsInMm(trackDataList, this.zCalc);
        StatsProcessing.recalculateVelocity(trackDataList, this.zCalc);
        point.setFastestStrikes();  // important to call this AFTER velocity has been recalculated
        if (points.isEmpty() && !games.isEmpty() && scoreLeft + scoreRight > 1) {
            GameData lastGame = games.get(games.size() - 1);
            lastGame.getPoints().add(point);
            games.set(games.size() - 1, new GameData(lastGame.getPoints()));
        } else points.add(point);
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
            GameData stats = new GameData(this.points);
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

    public MatchData createStats() {
        return new MatchData(games, playerNames.get(Side.LEFT), playerNames.get(Side.RIGHT), formattedMatchStart, tableCorners);
    }
}
