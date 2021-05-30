package ch.m3ts.eventbus.event.ball;

import ch.m3ts.detection.EventDetectionListener;
import ch.m3ts.util.Side;

public class StrikerSideChangeData implements EventDetectorEventData {
    private final Side striker;

    public StrikerSideChangeData(Side striker) {
        this.striker = striker;
    }

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onSideChange(striker);
    }
}
