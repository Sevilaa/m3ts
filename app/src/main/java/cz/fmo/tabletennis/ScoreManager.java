package cz.fmo.tabletennis;

import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

public class ScoreManager {
    private Map<Side, Integer> pointsMap;
    private Deque<Score> scores;
    private Side startServingSide;

    public ScoreManager(Side startServingSide) {
        this.startServingSide = startServingSide;
        this.pointsMap = new EnumMap<>(Side.class);
        pointsMap.put(Side.LEFT, 0);
        pointsMap.put(Side.RIGHT, 0);
        this.scores = new LinkedList<>();
    }

    public void score(Side winner, Side server) {
        Score score = new Score(server);
        score.setWinner(winner);
        updatePoints(winner, true);
        this.scores.addLast(score);
    }

    /**
     * Reverts the score by one (-1 manipulation).
     * @return server of the previous round
     */
    public Side revertLastScore(Side player) {
        if(!this.scores.isEmpty()) {
            this.scores.removeLast();
            updatePoints(player, false);
        }
        return getLastServer();
    }

    public int getScore(Side player) {
        return pointsMap.get(player);
    }

    private Side getLastServer() {
        Score score = scores.peekLast();
        Side server = startServingSide;
        if (score != null) {
            server = score.getServer();
        }
        return server;
    }

    /**
     * Updates points of the provided player by +-1
     * @param player specifies to adjust the points of
     * @param isIncrease specifies a +1 or -1 manipulation
     */
    private void updatePoints(Side player, boolean isIncrease) {
        int currentPoints = pointsMap.get(player);
        if (isIncrease) {
            currentPoints++;
        } else if (currentPoints != 0) {
            currentPoints--;
        }
        pointsMap.put(player, currentPoints);
    }
}
