package ch.m3ts.eventbus.event.todisplay;

import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import ch.m3ts.util.Side;

public class ReadyToServeData implements ToDisplayData {
    private final Side server;

    public ReadyToServeData(Side server) {
        this.server = server;
    }

    @Override
    public void call(DisplayUpdateListener displayUpdateListener) {
        displayUpdateListener.onReadyToServe(server);
    }
}
