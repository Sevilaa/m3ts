package ch.m3ts.tracker.init;

public interface InitTrackerCallback {
    byte[] onCaptureFrame();
    void setTableCorners(int[] tableCorners);
    void switchToLiveActivity(int matchType, int server);
    int getCameraHeight();
    int getCameraWidth();
    void updateLoadingBar(int partNumber);
    void setLoadingBarSize(int size);
    void frameSent();
}
