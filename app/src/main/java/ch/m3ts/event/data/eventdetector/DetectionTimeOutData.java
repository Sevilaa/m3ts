package ch.m3ts.event.data.eventdetector;

import ch.m3ts.tabletennis.events.EventDetectionListener;

public class DetectionTimeOutData implements EventDetectorEventData {

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onTimeout();
    }
}
