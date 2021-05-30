package ch.m3ts.eventbus.event;

import ch.m3ts.util.Side;

public class GestureData {
    private Side server;

    public GestureData(Side server) {
        this.server = server;
    }

    public Side getServer() {
        return server;
    }
}
