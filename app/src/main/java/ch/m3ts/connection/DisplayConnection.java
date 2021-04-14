package ch.m3ts.connection;

import android.graphics.Point;

import ch.m3ts.display.DisplayConnectCallback;
import ch.m3ts.display.DisplayScoreEventCallback;
import ch.m3ts.tabletennis.helper.Side;

public interface DisplayConnection {

    void setDisplayScoreEventCallback(DisplayScoreEventCallback displayScoreCallback);

    void setDisplayConnectCallback(DisplayConnectCallback displayConnectCallback);

    void onRequestTableFrame();

    void onSelectTableCorners(Point[] tableCorners);

    void onStartMatch(String matchType, String server);

    void onRestartMatch();

    void requestStatusUpdate();

    void onPause();

    void onResume();

    void onPointDeduction(Side side);

    void onPointAddition(Side side);
}
