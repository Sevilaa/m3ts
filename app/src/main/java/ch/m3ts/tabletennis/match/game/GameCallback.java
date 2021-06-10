package ch.m3ts.tabletennis.match.game;

import ch.m3ts.util.Side;

public interface GameCallback {
    void onPoint(Side side);

    void onPointDeduction(Side side);
}