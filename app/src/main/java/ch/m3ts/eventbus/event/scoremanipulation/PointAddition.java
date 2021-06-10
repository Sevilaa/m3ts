package ch.m3ts.eventbus.event.scoremanipulation;

import ch.m3ts.tabletennis.match.game.ScoreManipulationListener;
import ch.m3ts.util.Side;

public class PointAddition implements ScoreManipulationData {
    private Side side;

    public PointAddition(Side side) {
        this.side = side;
    }

    @Override
    public void call(ScoreManipulationListener scoreManipulationListener) {
        scoreManipulationListener.onPointAddition(side);
    }
}
