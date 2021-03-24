package ch.m3ts.connection;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchStatusCallback;
import ch.m3ts.tabletennis.match.game.ScoreManipulationCallback;
import ch.m3ts.tracker.init.InitTrackerCallback;
import ch.m3ts.tracker.visualization.MatchVisualizeHandlerCallback;

public interface TrackerConnection {
    void setTrackerPubNubCallback(MatchStatusCallback callback);
    void setScoreManipulationCallback(ScoreManipulationCallback scoreManipulationCallback);
    void setInitTrackerCallback(InitTrackerCallback initTrackerCallback);
    void setMatchVisualizeHandlerCallback(MatchVisualizeHandlerCallback matchVisualizeHandlerCallback);
    void sendStatusUpdate(String playerNameLeft, String playerNameRight, int scoreLeft, int scoreRight, int winsLeft, int winsRight, Side nextServer, int gamesNeededToWin);
}
