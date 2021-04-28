package ch.m3ts.display.stats;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.m3ts.tabletennis.helper.Side;

public class MatchStats implements Serializable {
    private final List<GameStats> gameStats;
    private int duration;
    private int averagePointDuration;
    private int amountOfCorrections;
    private int amountOfPoints;
    private String formattedTimestamp;
    private Map<Side, String> playerNames;
    private Map<Side, Float> fastestStrikes;
    private Map<Side, Integer> strikes;
    private final Map<Side, Integer> wins;

    public MatchStats(List<GameStats> gameStats, String playerLeft, String playerRight, String matchStart) {
        this.gameStats = gameStats;
        this.formattedTimestamp = matchStart;
        this.playerNames = new HashMap<>();
        this.wins = new HashMap<>();
        this.wins.put(Side.LEFT, 0);
        this.wins.put(Side.RIGHT, 0);
        this.strikes = new HashMap<>();
        this.strikes.put(Side.LEFT, 0);
        this.strikes.put(Side.RIGHT, 0);
        this.fastestStrikes = new HashMap<>();
        this.fastestStrikes.put(Side.LEFT, 0f);
        this.fastestStrikes.put(Side.RIGHT, 0f);
        playerNames.put(Side.LEFT, playerLeft);
        playerNames.put(Side.RIGHT, playerRight);
        calculateStatistics();
    }

    private void calculateStatistics() {
        averagePointDuration = amountOfPoints = amountOfCorrections = duration = 0;
        for (GameStats game : gameStats) {
            duration += game.getDuration();
            amountOfCorrections += game.getAmountOfCorrections();
            amountOfPoints += game.getAmountOfPoints();
            Side winner = game.getWinner();
            wins.put(winner, wins.get(winner) + 1);
            updateFastestStrike(game, Side.LEFT);
            updateFastestStrike(game, Side.RIGHT);
            this.strikes.put(Side.LEFT, this.strikes.get(Side.LEFT) + game.getPlayerStats().get(Side.LEFT).getStrikes());
            this.strikes.put(Side.RIGHT, this.strikes.get(Side.RIGHT) + game.getPlayerStats().get(Side.RIGHT).getStrikes());
        }
        if (gameStats.size() > 0) {
            averagePointDuration = duration / amountOfPoints;
        }
    }

    private void updateFastestStrike(GameStats game, Side side) {
        float fastest = game.getPlayerStats(side).getFastestStrike();
        if (fastest > fastestStrikes.get(side)) fastestStrikes.put(side, fastest);
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public int getDuration() {
        return duration;
    }

    public int getAveragePointDuration() {
        return averagePointDuration;
    }

    public String getPlayerName(Side side) {
        return playerNames.get(side);
    }

    public Map<Side, String> getPlayerNames() {
        return playerNames;
    }

    public float getErrorRatePercentage() {
        if (amountOfPoints == 0) return 0;
        return (float) amountOfCorrections / amountOfPoints * 100;
    }

    public List<GameStats> getGameStats() {
        return gameStats;
    }

    public int getWins(Side side) {
        return wins.get(side);
    }

    public Map<Side, Integer> getWins() {
        return wins;
    }

    public float getFastestStrike(Side side) {
        return fastestStrikes.get(side);
    }

    public Map<Side, Integer> getStrikes() {
        return strikes;
    }

    public int getAmountOfCorrections() {
        return amountOfCorrections;
    }

    public int getAmountOfPoints() {
        return amountOfPoints;
    }

    public Map<Side, Float> getFastestStrikes() {
        return fastestStrikes;
    }
}
