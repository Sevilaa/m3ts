package cz.fmo.tabletennis;

public interface ScoreManipulationCallback {
    void onPointDeduction(Side side);
    void onPointAddition(Side side);
}
