package ch.m3ts.tabletennis.match.referee;

/**
 * Currently the referee states are as follows:
 * - PLAY -> both players are currently playing the game
 * - SERVING -> a player is serving (when the ball is still on the servers' side)
 * - WAIT_FOR_SERVE -> a player will be serving shortly
 * - PAUSE -> a player just scored
 * - OUT_OF_FRAME -> the ball is not inside the frame anymore, the referee needs to wait and see
 */
public enum State {
    SERVING,
    PLAY,
    PAUSE,
    WAIT_FOR_SERVE,
    OUT_OF_FRAME
}
