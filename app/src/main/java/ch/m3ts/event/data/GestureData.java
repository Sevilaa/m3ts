package ch.m3ts.event.data;

import ch.m3ts.tabletennis.helper.Side;

public class GestureData {
    private Side server;

    public GestureData(Side server) {
        this.server = server;
    }

    public Side getServer() {
        return server;
    }
}
