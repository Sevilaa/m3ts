package com.android.grafika;

import android.support.annotation.NonNull;

import cz.fmo.Lib;
import cz.fmo.camera.CameraThread;

public class LiveDebugHandler extends DebugHandler implements CameraThread.Callback {
    private static final int CAMERA_ERROR = 2;

    public LiveDebugHandler(@NonNull DebugActivity activity) {
        super(activity);
    }

    @Override
    public void onCameraRender() {
        LiveDebugActivity activity = (LiveDebugActivity) mActivity.get();
        if (activity == null) return;
        if (activity.getmEncode() == null) return;
        activity.getmEncode().getHandler().sendFlush();
    }

    @Override
    public void onCameraFrame(byte[] dataYUV420SP) {
        Lib.detectionFrame(dataYUV420SP);
    }

    @Override
    public void onCameraError() {
        if (hasMessages(CAMERA_ERROR)) return;
        sendMessage(obtainMessage(CAMERA_ERROR));
    }
}
