package ch.m3ts.tabletennis.match.game;

public enum GameType {
    G11(11),
    G21(21);

    public final int amountOfPoints;

    GameType(int amountOfPoints) {
        this.amountOfPoints = amountOfPoints;
    }
}
