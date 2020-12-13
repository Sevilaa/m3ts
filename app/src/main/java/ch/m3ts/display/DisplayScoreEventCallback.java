package ch.m3ts.display;

import ch.m3ts.tabletennis.helper.Side;

public interface DisplayScoreEventCallback {
    void onStatusUpdate(String playerNameLeft, String playerNameRight, int pointsLeft, int pointsRight, int gamesLeft, int gamesRight, Side nextServer);
}
