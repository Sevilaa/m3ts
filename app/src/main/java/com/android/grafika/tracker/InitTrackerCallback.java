package com.android.grafika.tracker;

public interface InitTrackerCallback {
    byte[] onCaptureFrame();
    void setTableCorners(int[] tableCorners);
    void switchToDebugActivity();
    int getCameraHeight();
    int getCameraWidth();
    void updateLoadingBar(int partNumber);
    void setLoadingBarSize(int size);
    void frameSent();
}
