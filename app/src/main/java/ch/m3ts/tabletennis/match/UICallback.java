package ch.m3ts.tabletennis.match;

import ch.m3ts.tabletennis.helper.Side;

/**
 * Provides methods on which a Table Tennis Scoreboard needs to be refreshed on.
 */
public interface UICallback {
    void onMatchEnded(String winnerName);
    void onScore (Side side, int score, Side nextServer, Side lastServer);
    void onWin (Side side, int wins);
    void onReadyToServe(Side server);
}
