package cz.fmo.tabletennis;

public enum GameType {
    G11(11),
    G21(21);

    public final int amountOfPoints;

    GameType(int amountOfPoints) {
        this.amountOfPoints = amountOfPoints;
    }
}
