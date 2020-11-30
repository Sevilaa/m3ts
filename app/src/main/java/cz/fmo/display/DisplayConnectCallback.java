package cz.fmo.display;

import org.json.JSONObject;

public interface DisplayConnectCallback {
    void onImageReceived(byte[] imageBytes);
    void onConnected();
}
