package ch.m3ts.eventbus.event.scoremanipulation;

import ch.m3ts.tabletennis.match.game.ScoreManipulationListener;

public class ResumeMatch implements ScoreManipulationData {
    @Override
    public void call(ScoreManipulationListener scoreManipulationListener) {
        scoreManipulationListener.onResume();
    }
}
