package ch.m3ts.eventbus.event.todisplay;

import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import ch.m3ts.util.Side;

public class ToDisplayGameWinData implements ToDisplayData {
    private final Side winner;
    private final int wins;

    public ToDisplayGameWinData(Side winner, int wins) {
        this.winner = winner;
        this.wins = wins;
    }

    @Override
    public void call(DisplayUpdateListener displayUpdateListener) {
        displayUpdateListener.onWin(winner, wins);
    }
}