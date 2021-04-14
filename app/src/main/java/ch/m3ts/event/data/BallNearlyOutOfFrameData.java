package ch.m3ts.event.data;

import ch.m3ts.tabletennis.events.EventDetectionListener;
import ch.m3ts.tabletennis.helper.Side;
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
