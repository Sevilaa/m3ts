package ch.m3ts.detection;

import ch.m3ts.util.Table;

/**
 * Predicts where a ball (detection / track) will move to in future frames
 */
public interface BallCurvePredictor {
    boolean willBallMoveIntoNet(int[] cx, int[] cy, Table table);
}
