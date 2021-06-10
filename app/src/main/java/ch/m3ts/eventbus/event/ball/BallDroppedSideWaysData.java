package ch.m3ts.eventbus.event.ball;

import ch.m3ts.detection.EventDetectionListener;

public class BallDroppedSideWaysData implements EventDetectorEventData {

    public BallDroppedSideWaysData() {
        // no data needed in this specific event
    }

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onBallDroppedSideWays();
    }
}