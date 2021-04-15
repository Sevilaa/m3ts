package ch.m3ts.event.data.todisplay;

import ch.m3ts.tabletennis.match.DisplayUpdateListener;

public class MatchEndedData implements ToDisplayData {
    private final String winnerName;

    public MatchEndedData(String winnerName) {
        this.winnerName = winnerName;
    }

    @Override
    public void call(DisplayUpdateListener displayUpdateListener) {
        displayUpdateListener.onMatchEnded(winnerName);
    }
}
