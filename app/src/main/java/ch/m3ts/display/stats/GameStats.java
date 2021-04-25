package ch.m3ts.display.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.m3ts.tabletennis.helper.Side;

public class GameStats implements Serializable {
    private int duration;
    private int amountOfCorrections;
    private int amountOfPoints;
    private final List<PointData> points;
    private Map<Side, PlayerStats> playerStats;
    private Side winner;
    private int averagePointDuration;

    public GameStats(List<PointData> points) {
        this.points = points;
        this.playerStats = new HashMap<>();
        calculateStatistics();
    }

    private void calculateStatistics() {
        int duration, corrections, strikesLeft, strikesRight;
        float fastestStrikeLeft, fastestStrikeRight;
        fastestStrikeLeft = fastestStrikeRight = 0;
        strikesLeft = strikesRight = duration = corrections = 0;

        for (PointData point : points) {
            if (point.isCorrection()) {
                corrections++;
                continue;
            }
            strikesLeft += point.getStrikes().get(Side.LEFT);
            strikesRight += point.getStrikes().get(Side.RIGHT);
            duration += point.getDuration();
            Map<Side, Float> fastestStrikes = point.getFastestStrikes();
            if (fastestStrikeLeft < fastestStrikes.get(Side.LEFT))
                fastestStrikeLeft = fastestStrikes.get(Side.LEFT);
            if (fastestStrikeRight < fastestStrikes.get(Side.RIGHT))
                fastestStrikeRight = fastestStrikes.get(Side.RIGHT);

        }

        PointData lastPoint = points.get(points.size() - 1);
        amountOfPoints = lastPoint.getScore(Side.LEFT) + lastPoint.getScore(Side.RIGHT);
        playerStats.put(Side.LEFT, new PlayerStats(Side.LEFT, strikesLeft, fastestStrikeLeft, lastPoint.getScore(Side.LEFT)));
        playerStats.put(Side.RIGHT, new PlayerStats(Side.RIGHT, strikesRight, fastestStrikeRight, lastPoint.getScore(Side.RIGHT)));
        winner = lastPoint.getScore(Side.LEFT) > lastPoint.getScore(Side.RIGHT) ? Side.LEFT : Side.RIGHT;
        amountOfCorrections = corrections;
        this.duration = duration;
        if (points.size() > 0) {
            averagePointDuration = duration / amountOfPoints;
        }
    }

    public int getDuration() {
        return duration;
    }

    public int getAveragePointDuration() {
        return averagePointDuration;
    }

    public double getErrorRatePercentage() {
        if (amountOfPoints == 0) return 0;
        return (double) amountOfCorrections / amountOfPoints * 100;
    }

    public List<PointData> getPoints() {
        return points;
    }

    public PlayerStats getPlayerStats(Side side) {
        return playerStats.get(side);
    }

    public int getAmountOfCorrections() {
        return amountOfCorrections;
    }

    public int getAmountOfPoints() {
        return amountOfPoints;
    }

    public Side getWinner() {
        return winner;
    }

    public Map<Side, PlayerStats> getPlayerStats() {
        return playerStats;
    }
}
