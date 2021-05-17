package ch.m3ts.tabletennis.match.game;

import ch.m3ts.display.stats.StatsCreator;
import ch.m3ts.eventbus.EventBus;
import ch.m3ts.eventbus.TTEvent;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.data.game.GameWinData;
import ch.m3ts.eventbus.data.game.GameWinResetData;
import ch.m3ts.eventbus.data.todisplay.ScoreData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.ServeRules;

/**
 * Represents a game inside of a table tennis match.
 * Provides information on the current score and the current server.
 * <p>
 * A game usually ends when one player has reached eleven points.
 * However, there is an overtime mechanism when both players reach ten points.
 */
public class Game implements GameCallback {
    private final ScoreManager scoreManager;
    private Side server;
    private final GameType type;
    private final ServeRules serveRules;
    private final EventBus eventBus;

    public Game(GameType type, ServeRules serveRules, Side server) {
        scoreManager = new ScoreManager(server, type);
        this.server = server;
        this.type = type;
        this.serveRules = serveRules;
        this.eventBus = TTEventBus.getInstance();
    }

    @Override
    public void onPoint(Side side) {
        scoreManager.score(side, server);
        int score = scoreManager.getScore(side);
        Side lastServer = server;
        changeServer();
        this.eventBus.dispatch(new TTEvent<>(new ScoreData(side, score, this.server, lastServer)));
        if (this.scoreManager.hasReachedMax(score)) {
            StatsCreator.getInstance().addGame();
            this.eventBus.dispatch(new TTEvent<>(new GameWinData(side)));
        }
    }

    @Override
    public void onPointDeduction(Side side) {
        int score = scoreManager.getScore(side);
        if (score > 0) {
            Side lastServer = scoreManager.revertLastScore(side);
            score = scoreManager.getScore(side);
            this.server = lastServer;
            this.eventBus.dispatch(new TTEvent<>(new ScoreData(side, score, this.server, lastServer)));
            if (this.scoreManager.hasReachedMax(scoreManager.getScore(Side.getOpposite(side)))) {
                this.eventBus.dispatch(new TTEvent<>(new GameWinData(Side.getOpposite(side))));
            }
        } else if (getSumOfScores() == 0) {
            this.eventBus.dispatch(new TTEvent<>(new GameWinResetData()));
        }
    }

    public Side getServer() {
        return this.server;
    }

    public int getScore(Side player) {
        return scoreManager.getScore(player);
    }

    private int getSumOfScores() {
        return this.scoreManager.getScore(Side.LEFT) + this.scoreManager.getScore(Side.RIGHT);
    }

    private boolean isOneServeRuleActive() {
        return getSumOfScores() >= 2 * this.type.amountOfPoints - 2;
    }

    private void changeServer() {
        int sumOfScores = getSumOfScores();
        if (sumOfScores % this.serveRules.amountOfServes == 0 ||
                isOneServeRuleActive()) {
            server = Side.getOpposite(server);
        }
    }
}
