package ch.m3ts.detection;


import ch.m3ts.util.Side;

public interface GestureCallback {
    void onWaitingForGesture(Side server);
}
