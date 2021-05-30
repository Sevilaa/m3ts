package ch.m3ts.connection;

import ch.m3ts.tabletennis.match.MatchStatusCallback;
import ch.m3ts.tracker.init.InitTrackerCallback;
import ch.m3ts.util.Side;

public interface TrackerConnection {
    void setTrackerPubNubCallback(MatchStatusCallback callback);

    void setInitTrackerCallback(InitTrackerCallback initTrackerCallback);

    void sendStatusUpdate(String playerNameLeft, String playerNameRight, int scoreLeft, int scoreRight, int winsLeft, int winsRight, Side nextServer, int gamesNeededToWin);
}
