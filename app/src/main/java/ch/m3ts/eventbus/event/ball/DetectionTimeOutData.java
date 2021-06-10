package ch.m3ts.eventbus.event.ball;

import ch.m3ts.detection.EventDetectionListener;

public class DetectionTimeOutData implements EventDetectorEventData {

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onTimeout();
    }
}
