package ch.m3ts.tabletennis.events;

import ch.m3ts.tabletennis.Table;

/**
 * Predicts where a ball (detection / track) will move to in future frames
 */
public interface BallCurvePredictor {
    boolean willBallMoveIntoNet(int[] cx, int[] cy, Table table);
}
