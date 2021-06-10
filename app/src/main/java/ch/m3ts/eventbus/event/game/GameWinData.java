package ch.m3ts.eventbus.event.game;

import ch.m3ts.tabletennis.match.GameListener;
import ch.m3ts.util.Side;

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
