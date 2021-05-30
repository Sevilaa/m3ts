package ch.m3ts.tabletennis.match;

import ch.m3ts.util.Side;

/**
 * Represents the status of a match.
 */
public class MatchStatus {
    private final String playerLeft;
    private final String playerRight;
    private final int scoreLeft;
    private final int scoreRight;
    private final Side nextServer;
    private final int gamesNeededToWin;
    private final int winsLeft;
    private final int winsRight;

    public MatchStatus(String playerLeft, String playerRight, int scoreLeft, int scoreRight, int winsLeft, int winsRight, Side nextServer, int gamesNeededToWin) {
        this.playerLeft = playerLeft;
        this.playerRight = playerRight;
        this.scoreLeft = scoreLeft;
        this.scoreRight = scoreRight;
        this.winsLeft = winsLeft;
        this.winsRight = winsRight;
        this.nextServer = nextServer;
        this.gamesNeededToWin = gamesNeededToWin;
    }

    public Side getNextServer() {
        return nextServer;
    }

    public String getPlayerLeft() {
        return playerLeft;
    }

    public String getPlayerRight() {
        return playerRight;
    }

    public int getScoreLeft() {
        return scoreLeft;
    }

    public int getScoreRight() {
        return scoreRight;
    }

    public int getWinsLeft() {
        return winsLeft;
    }

    public int getWinsRight() {
        return winsRight;
    }

    public int getGamesNeededToWin() {
        return this.gamesNeededToWin;
    }
}
