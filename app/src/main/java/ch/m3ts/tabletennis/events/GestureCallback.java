package ch.m3ts.tabletennis.events;


import ch.m3ts.tabletennis.helper.Side;

public interface GestureCallback {
    void onWaitingForGesture(Side server);
}
