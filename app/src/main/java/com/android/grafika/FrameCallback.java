package com.android.grafika;

public interface FrameCallback {
    void onFrame(byte[] dataYUV420SP);
    byte[] onCaptureFrame();
}
