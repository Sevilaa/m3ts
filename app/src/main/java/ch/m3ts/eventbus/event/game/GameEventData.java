package ch.m3ts.eventbus.event.game;

import ch.m3ts.tabletennis.match.GameListener;

/**
 * Events dispatched by m3ts.tabletennis.match.game.Game class
 */
public interface GameEventData {
    void call(GameListener gameListener);
}
