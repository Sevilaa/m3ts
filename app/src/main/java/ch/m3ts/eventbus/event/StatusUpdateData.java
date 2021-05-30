package ch.m3ts.eventbus.event;

import ch.m3ts.tabletennis.match.MatchStatus;
import ch.m3ts.util.Side;

public class StatusUpdateData {
    private String playerNameLeft;
    private String playerNameRight;
    private int pointsLeft;
    private int pointsRight;
    private int gamesLeft;
    private int gamesRight;
    private Side nextServer;
    private int gamesNeededToWin;

    public StatusUpdateData(String playerNameLeft, String playerNameRight, int pointsLeft, int pointsRight, int gamesLeft, int gamesRight, Side nextServer, int gamesNeededToWin) {
        this.playerNameLeft = playerNameLeft;
        this.playerNameRight = playerNameRight;
        this.pointsLeft = pointsLeft;
        this.pointsRight = pointsRight;
        this.gamesLeft = gamesLeft;
        this.gamesRight = gamesRight;
        this.nextServer = nextServer;
        this.gamesNeededToWin = gamesNeededToWin;
    }

    public StatusUpdateData(MatchStatus status) {
        this(status.getPlayerLeft(), status.getPlayerRight(), status.getScoreLeft(), status.getScoreRight(), status.getWinsLeft(), status.getWinsRight(), status.getNextServer(), status.getGamesNeededToWin());
    }

    public int getGamesNeededToWin() {
        return gamesNeededToWin;
    }

    public String getPlayerNameLeft() {
        return playerNameLeft;
    }

    public String getPlayerNameRight() {
        return playerNameRight;
    }

    public int getPointsLeft() {
        return pointsLeft;
    }

    public int getPointsRight() {
        return pointsRight;
    }

    public int getGamesLeft() {
        return gamesLeft;
    }

    public int getGamesRight() {
        return gamesRight;
    }

    public Side getNextServer() {
        return nextServer;
    }
}
