package ch.m3ts.tracker.visualization;

import android.os.Handler;

import cz.fmo.camera.CameraThread;

/**
 * Camera callback class which does nothing when a camera frame is rendered.
 * Only used for "demonstration" purposes in CameraPreviewActivity.
 */
public class DummyCameraCallback extends Handler implements CameraThread.Callback {

    @Override
    public void onCameraRender() {
        // do nothing
    }

    @Override
    public void onCameraFrame(byte[] dataYUV420SP) {
        // do nothing
    }

    @Override
    public void onCameraError() {
        // do nothing
    }
}
