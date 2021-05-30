package ch.m3ts.tabletennis.match.game;

import ch.m3ts.util.Side;

public interface ScoreManipulationListener {
    void onPointDeduction(Side side);

    void onPointAddition(Side side);

    void onPause();

    void onResume();
}
