package cz.fmo.display;

import org.json.JSONObject;

public interface DisplayEventCallback {
    void onStatusUpdate(String playerNameLeft, String playerNameRight, int pointsLeft, int pointsRight, int gamesLeft, int gamesRight);
    void onImageReceived(JSONObject jsonObject);
}
