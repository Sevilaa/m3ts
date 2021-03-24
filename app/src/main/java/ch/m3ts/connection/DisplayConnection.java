package ch.m3ts.connection;

import android.graphics.Point;

import ch.m3ts.display.DisplayConnectCallback;
import ch.m3ts.display.DisplayScoreEventCallback;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.UICallback;

public interface DisplayConnection {
    void setUiCallback(UICallback uiCallback);
    void setDisplayScoreEventCallback(DisplayScoreEventCallback displayScoreCallback);
    void setDisplayConnectCallback(DisplayConnectCallback displayConnectCallback);
    void onRequestTableFrame();
    void onSelectTableCorners(Point[] tableCorners);
    void onStartMatch();
    void onRestartMatch();
    void requestStatusUpdate();
    void onPause();
    void onResume();
    void onPointDeduction(Side side);
    void onPointAddition(Side side);
}
