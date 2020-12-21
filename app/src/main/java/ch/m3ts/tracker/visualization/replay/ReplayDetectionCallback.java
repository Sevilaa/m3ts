package ch.m3ts.tracker.visualization.replay;

public interface ReplayDetectionCallback {
    void onEncodedFrame(byte[] dataYUV420SP);
}
