package ch.m3ts.eventbus.event.ball;

import ch.m3ts.detection.EventDetectionListener;
import ch.m3ts.util.Side;
import cz.fmo.Lib;

public class BallNearlyOutOfFrameData implements EventDetectorEventData {
    private final Lib.Detection detection;
    private final Side side;

    public BallNearlyOutOfFrameData(Lib.Detection detection, Side side) {
        this.detection = detection;
        this.side = side;
    }

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onNearlyOutOfFrame(detection, side);
    }
}
