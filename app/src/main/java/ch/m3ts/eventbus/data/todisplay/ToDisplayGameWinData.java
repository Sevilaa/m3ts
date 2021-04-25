package ch.m3ts.eventbus.data.todisplay;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;

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