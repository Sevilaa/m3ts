package ch.m3ts.event.data.eventdetector;

import ch.m3ts.tabletennis.events.EventDetectionListener;

public class BallDroppedSideWaysData implements EventDetectorEventData {

    public BallDroppedSideWaysData() {
        // no data needed in this specific event
    }

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onBallDroppedSideWays();
    }
}