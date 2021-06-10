package ch.m3ts.eventbus.event.ball;

import ch.m3ts.detection.EventDetectionListener;
import ch.m3ts.util.Side;

public class TableSideChangeData implements EventDetectorEventData {
    private final Side tableSide;

    public TableSideChangeData(Side tableSide) {
        this.tableSide = tableSide;
    }

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onTableSideChange(tableSide);
    }
}
