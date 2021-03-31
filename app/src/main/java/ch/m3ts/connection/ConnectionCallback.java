package ch.m3ts.connection;

import com.google.android.gms.nearby.connection.PayloadCallback;

public interface ConnectionCallback {
    void onDiscoverFailure();
    void onRejection();
    void onConnection(String endpoint);
    void onConnecting(String endpointId, String endpointName, String token, PayloadCallback callback);
    void onDisconnection(String endpoint);
}
