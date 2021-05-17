package ch.m3ts.eventbus.data.eventdetector;

import ch.m3ts.tabletennis.events.EventDetectionListener;

public interface EventDetectorEventData {
    void call(EventDetectionListener eventDetectionListener);
}
