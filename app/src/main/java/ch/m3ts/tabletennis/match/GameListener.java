package ch.m3ts.tabletennis.match;

import ch.m3ts.tabletennis.helper.Side;

public interface GameListener {
    void onGameWin(Side side);
}
