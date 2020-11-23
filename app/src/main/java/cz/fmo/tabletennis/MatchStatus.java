package cz.fmo.tabletennis;

public class MatchStatus {
    private String playerLeft;
    private String playerRight;
    private int scoreLeft;
    private int scoreRight;

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

    private int winsLeft;
    private int winsRight;

    public MatchStatus(String playerLeft, String playerRight, int scoreLeft, int scoreRight, int winsLeft, int winsRight) {
        this.playerLeft = playerLeft;
        this.playerRight = playerRight;
        this.scoreLeft = scoreLeft;
        this.scoreRight = scoreRight;
        this.winsLeft = winsLeft;
        this.winsRight = winsRight;
    }
}
