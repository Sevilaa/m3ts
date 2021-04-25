package ch.m3ts.eventbus.data.eventdetector;

import ch.m3ts.tabletennis.events.EventDetectionListener;
import ch.m3ts.tabletennis.helper.Side;
import cz.fmo.Lib;

public class BallBounceData implements EventDetectorEventData {
    private final Lib.Detection detection;
    private final Side tableSide;

    public BallBounceData(Lib.Detection detection, Side tableSide) {
        this.detection = detection;
        this.tableSide = tableSide;
    }

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onBounce(detection, tableSide);
    }
}
