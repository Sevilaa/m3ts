package ch.m3ts.eventbus.data.todisplay;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;

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
