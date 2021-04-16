package ch.m3ts.event.data.game;

import ch.m3ts.tabletennis.match.GameListener;

public class GameWinResetData implements GameEventData {
    public GameWinResetData() {

    }

    @Override
    public void call(GameListener gameListener) {
        gameListener.onGameWinReset();
    }
}
