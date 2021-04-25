package ch.m3ts.eventbus.data.scoremanipulation;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.game.ScoreManipulationListener;

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
