package ch.m3ts.event.data.game;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.GameListener;

public class GameWinData implements GameEventData {
    private final Side winnerSide;

    public GameWinData(Side winnerSide) {
        this.winnerSide = winnerSide;
    }

    @Override
    public void call(GameListener gameListener) {
        gameListener.onGameWin(winnerSide);
    }
}
