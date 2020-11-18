package cz.fmo.tabletennis;

public interface UICallback {
    void onMatchEnded(String winnerName);
    void onScore (Side side, int score);
    void onWin (Side side, int wins);
}
