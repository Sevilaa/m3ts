package ch.m3ts.tabletennis.match;

import ch.m3ts.util.Side;

public interface GameListener {
    void onGameWin(Side side);

    void onGameWinReset();
}
