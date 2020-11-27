package cz.fmo.tabletennis;

public class Game implements GameCallback {
    private int maxScore;
    private ScoreManager scoreManager;
    private MatchCallback matchCallback;
    private UICallback uiCallback;
    private Side server;
    private GameType type;
    private ServeRules serveRules;

    public Game (MatchCallback matchCallback, UICallback uiCallback, GameType type, ServeRules serveRules, Side server) {
        scoreManager = new ScoreManager(server);
        this.server = server;
        this.matchCallback = matchCallback;
        this.uiCallback = uiCallback;
        this.type = type;
        this.maxScore = type.amountOfPoints;
        this.serveRules = serveRules;
    }

    @Override
    public void onPoint(Side side) {
        scoreManager.score(side, server);
        int score = scoreManager.getScore(side);
        changeServer();
        uiCallback.onScore(side, score, this.server);
        if (hasReachedMax(score)) {
            matchCallback.onWin(side);
        }
    }

    @Override
    public void onPointDeduction(Side side) {
        int score = scoreManager.getScore(side);
        if (score > 0) {
            Side lastServer = scoreManager.revertLastScore(side);
            score = scoreManager.getScore(side);
            this.server = lastServer;
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

    public int getScore(Side player) {
        return scoreManager.getScore(player);
    }

    private boolean hasReachedMax(int score) {
        if ((scoreManager.getScore(Side.LEFT) == scoreManager.getScore(Side.RIGHT)) && (score == maxScore - 1)) {
            increaseMaxScore();
        }
        return (score >= maxScore);
    }

    private void increaseMaxScore() {
        maxScore++;
    }

    private int getSumOfScores() {
        return this.scoreManager.getScore(Side.LEFT) + this.scoreManager.getScore(Side.RIGHT);
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
