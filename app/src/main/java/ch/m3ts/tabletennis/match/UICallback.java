package ch.m3ts.tabletennis.match;

import ch.m3ts.tabletennis.helper.Side;

public interface UICallback {
    void onMatchEnded(String winnerName);
    void onScore (Side side, int score, Side nextServer);
    void onWin (Side side, int wins);
    void onReadyToServe(Side server);
}
