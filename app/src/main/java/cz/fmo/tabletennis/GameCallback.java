package cz.fmo.tabletennis;

public interface GameCallback {
    void onPoint(Side side);
    void onPointDeduction(Side side);
    void onReadyToServe(Side side);
}