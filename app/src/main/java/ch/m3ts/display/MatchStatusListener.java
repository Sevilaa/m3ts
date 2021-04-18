package ch.m3ts.display;

import ch.m3ts.tabletennis.helper.Side;

/**
 * Provides methods which let the display device request the current match status from the tracker
 * device.
 */
public interface MatchStatusListener {
    void onStatusUpdate(String playerNameLeft, String playerNameRight, int pointsLeft, int pointsRight, int gamesLeft, int gamesRight, Side nextServer, int gamesNeededToWin);
}
