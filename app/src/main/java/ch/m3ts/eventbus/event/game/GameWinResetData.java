package ch.m3ts.eventbus.event.game;

import ch.m3ts.tabletennis.match.GameListener;

public class GameWinResetData implements GameEventData {
    @Override
    public void call(GameListener gameListener) {
        gameListener.onGameWinReset();
    }
}
