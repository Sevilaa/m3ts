package ch.m3ts.tabletennis.match;

import ch.m3ts.util.Side;

/**
 * Provides methods on which a Table Tennis Scoreboard needs to be refreshed on.
 */
public interface DisplayUpdateListener {
    void onMatchEnded(String winnerName);

    void onScore(Side scorer, int score, Side nextServer, Side lastServer);

    void onWin(Side winner, int wins);

    void onReadyToServe(Side server);

    void onNotReadyButPlaying();
}