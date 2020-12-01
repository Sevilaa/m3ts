package cz.fmo.display;

import org.json.JSONObject;

public interface DisplayConnectCallback {
    void onImageReceived(byte[] imageBytes, int imageWidth, int imageHeight);
    void onConnected();
}
