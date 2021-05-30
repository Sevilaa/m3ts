package ch.m3ts.eventbus.event.ball;

import ch.m3ts.detection.EventDetectionListener;

public interface EventDetectorEventData {
    void call(EventDetectionListener eventDetectionListener);
}
