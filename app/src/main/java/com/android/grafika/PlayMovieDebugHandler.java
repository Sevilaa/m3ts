package com.android.grafika;

import android.support.annotation.NonNull;

import cz.fmo.Lib;

public class PlayMovieDebugHandler extends DebugHandler implements PlayMovieDetectionCallback {
    public PlayMovieDebugHandler(@NonNull DebugActivity activity) {
        super(activity);
    }

    @Override
    public void onEncodedFrame(byte[] dataYUV420SP) {
        try {
            Lib.detectionFrame(dataYUV420SP);
        } catch (Exception ex) {
            Log.e(ex.getMessage(), ex);
        }
    }
}
