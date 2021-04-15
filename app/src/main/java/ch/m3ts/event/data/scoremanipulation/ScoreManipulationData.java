package ch.m3ts.event.data.scoremanipulation;

import ch.m3ts.tabletennis.match.game.ScoreManipulationListener;

public interface ScoreManipulationData {
    void call(ScoreManipulationListener scoreManipulationListener);
}
