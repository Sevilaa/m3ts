package cz.fmo.tabletennis;

public interface UICallback {
    void onMatchEnded(String winnerName);
    void onScore (Side side, int score, Side nextServer);
    void onWin (Side side, int wins);
}
