package ch.m3ts.event.data.eventdetector;

import ch.m3ts.tabletennis.events.EventDetectionListener;
import ch.m3ts.tabletennis.helper.Side;

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
