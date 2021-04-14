package ch.m3ts.connection;

import android.graphics.Point;

import ch.m3ts.display.DisplayConnectCallback;

public interface DisplayConnection {
    void setDisplayConnectCallback(DisplayConnectCallback displayConnectCallback);

    void onRequestTableFrame();

    void onSelectTableCorners(Point[] tableCorners);

    void onStartMatch(String matchType, String server);

    void requestStatusUpdate();
}
