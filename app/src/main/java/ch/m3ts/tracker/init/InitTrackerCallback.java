package ch.m3ts.tracker.init;

public interface InitTrackerCallback {
    byte[] onCaptureFrame();
    void setTableCorners(int[] tableCorners);
    void switchToLiveActivity();
    int getCameraHeight();
    int getCameraWidth();
    void updateLoadingBar(int partNumber);
    void setLoadingBarSize(int size);
    void frameSent();
}
