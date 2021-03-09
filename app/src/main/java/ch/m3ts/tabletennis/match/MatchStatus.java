package ch.m3ts.tabletennis.match;

import ch.m3ts.tabletennis.helper.Side;

/**
 * Represents the status of a match.
 */
public class MatchStatus {
    private String playerLeft;
    private String playerRight;
    private int scoreLeft;
    private int scoreRight;
    private Side nextServer;
    private int gamesNeededToWin;

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

    private int winsLeft;
    private int winsRight;

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
}
