package com.android.grafika.tracker;

public interface InitTrackerCallback {
    byte[] onCaptureFrame();
    void setTableCorners(int[] tableCorners);
    void switchToDebugActivity();
}
