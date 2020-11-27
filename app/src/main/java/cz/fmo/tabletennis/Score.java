package cz.fmo.tabletennis;

public class Score {
    private Side server;
    private Side winner;

    public Score(Side server) {
        this.server = server;
        this.winner = null;
    }

    public void setWinner(Side winner) {
        this.winner = winner;
    }

    public Side getWinner() {
        return winner;
    }

    public Side getServer() {
        return server;
    }
}