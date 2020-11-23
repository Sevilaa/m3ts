package cz.fmo.display;

import org.json.JSONObject;

import cz.fmo.tabletennis.Side;

public interface DisplayEventCallback {
    void onStatusUpdate(String playerNameLeft, String playerNameRight, int pointsLeft, int pointsRight, int gamesLeft, int gamesRight, Side nextServer);
    void onImageReceived(JSONObject jsonObject);
}
