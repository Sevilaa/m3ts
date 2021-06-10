package ch.m3ts.eventbus.event.scoremanipulation;

import ch.m3ts.tabletennis.match.game.ScoreManipulationListener;
import ch.m3ts.util.Side;

public class PointDeduction implements ScoreManipulationData {
    private Side side;

    public PointDeduction(Side side) {
        this.side = side;
    }

    @Override
    public void call(ScoreManipulationListener scoreManipulationListener) {
        scoreManipulationListener.onPointDeduction(side);
    }
}
