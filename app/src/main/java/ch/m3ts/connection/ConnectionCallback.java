package ch.m3ts.connection;

public interface ConnectionCallback {
    void onDiscoverFailure();
    void onRejection();
    void onConnection(String endpoint);
    void onConnecting(String endpoint);
    void onDisconnection(String endpoint);
}
