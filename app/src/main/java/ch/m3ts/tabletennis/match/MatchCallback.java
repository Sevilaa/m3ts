package ch.m3ts.tabletennis.match;

import ch.m3ts.tabletennis.helper.Side;

public interface MatchCallback {
    void onWin(Side side);
}
