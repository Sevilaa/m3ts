package ch.m3ts.display;

/**
 * Provides methods which allow image transmissions from PubNub as images need to be split up
 * beforehand.
 */
public interface DisplayConnectCallback {
    void onImageReceived(byte[] imageBytes, int imageWidth, int imageHeight);

    void onConnected();

    void onImageTransmissionStarted(int parts);

    void onImagePartReceived(int partNumber);
}
