package ch.m3ts.event.data;

import ch.m3ts.tabletennis.events.EventDetectionListener;

public interface EventDetectorEventData {
    void call(EventDetectionListener eventDetectionListener);
}
