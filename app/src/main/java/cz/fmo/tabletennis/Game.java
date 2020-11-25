package cz.fmo.tabletennis;

import java.util.HashMap;
import java.util.Map;

public class Game implements GameCallback {
    private int maxScore;
    private Map<Side, Integer> scores;
    private MatchCallback matchCallback;
    private UICallback uiCallback;
    private Side server;
    private GameType type;
    private ServeRules serveRules;

    public Game (MatchCallback matchCallback, UICallback uiCallback, GameType type, ServeRules serveRules, Side server) {
        scores = new HashMap<>();
        scores.put(Side.LEFT, 0);
        scores.put(Side.RIGHT, 0);
        this.server = server;
        this.matchCallback = matchCallback;
        this.uiCallback = uiCallback;
        this.type = type;
        this.maxScore = type.amountOfPoints;
        this.serveRules = serveRules;
    }

    @Override
    public void onPoint(Side side) {
        int score = scores.get(side) + 1;
        scores.put(side, score);
        changeServer();
        uiCallback.onScore(side, score, this.server);
        if (hasReachedMax(score)) {
            matchCallback.onWin(side);
        }
    }

    @Override
    public void onPointDeduction(Side side) {
        int score = scores.get(side) - 1;
        if(score >= 0) {
            scores.put(side, score);
            changeServer();
            uiCallback.onScore(side, score, this.server);
            if (hasReachedMax(score)) {
                matchCallback.onWin(side);
            }
        }
    }

    @Override
    public void onReadyToServe(Side side) {
        uiCallback.onReadyToServe(side);
    }

    public Side getServer() {
        return this.server;
    }

    public int getScore(Side side) {
        return scores.get(side);
    }

    private boolean hasReachedMax(int score) {
        if (scores.get(Side.LEFT).equals(scores.get(Side.RIGHT)) && score == maxScore-1) {
            increaseMaxScore();
        }
        return (score >= maxScore);
    }

    private void increaseMaxScore() {
        maxScore++;
    }

    private int getSumOfScores() {
        return this.scores.get(Side.LEFT) + this.scores.get(Side.RIGHT);
    }

    private boolean isOneServeRuleActive() {
        return getSumOfScores() >= 2 * this.type.amountOfPoints - 2;
    }

    private void changeServer() {
        int sumOfScores = getSumOfScores();
        if(sumOfScores % this.serveRules.amountOfServes == 0 ||
            isOneServeRuleActive()) {
            if (server == Side.LEFT) {
                server = Side.RIGHT;
            } else {
                server = Side.LEFT;
            }
        }
    }
}
