package ch.m3ts.detection;

import ch.m3ts.util.Side;
import cz.fmo.Lib;
import cz.fmo.data.Track;

/**
 * Represent the events interpreted by the EventDetector.
 */
public interface EventDetectionListener {
    void onBounce(Lib.Detection detection, Side side);

    void onSideChange(Side side);

    void onNearlyOutOfFrame(Lib.Detection detection, Side side);

    void onStrikeFound(Track track);

    void onTableSideChange(Side side);

    void onBallDroppedSideWays();

    void onTimeout();

    void onAudioBounce(Side side);

    void onBallMovingIntoNet();
}
