package ch.m3ts.eventbus.data.eventdetector;

import ch.m3ts.tabletennis.events.EventDetectionListener;

public class DetectionTimeOutData implements EventDetectorEventData {

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onTimeout();
    }
}
