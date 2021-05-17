package ch.m3ts.eventbus.data.todisplay;

import ch.m3ts.tabletennis.match.DisplayUpdateListener;

public class InvalidServeData implements ToDisplayData {
    @Override
    public void call(DisplayUpdateListener displayUpdateListener) {
        displayUpdateListener.onNotReadyButPlaying();
    }
}
