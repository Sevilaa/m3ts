package ch.m3ts.event.data.todisplay;

import ch.m3ts.tabletennis.match.DisplayUpdateListener;

public class InvalidServeData implements ToDisplayData {
    @Override
    public void call(DisplayUpdateListener displayUpdateListener) {
        displayUpdateListener.onNotReadyButPlaying();
    }
}
