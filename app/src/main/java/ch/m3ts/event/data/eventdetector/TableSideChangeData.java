package ch.m3ts.event.data.eventdetector;

import ch.m3ts.tabletennis.events.EventDetectionListener;
import ch.m3ts.tabletennis.helper.Side;

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
