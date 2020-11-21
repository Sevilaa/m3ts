package com.android.grafika;

import android.support.annotation.NonNull;

import cz.fmo.Lib;
import cz.fmo.tabletennis.MatchType;
import cz.fmo.tabletennis.Side;

public class PlayMovieDebugHandler extends DebugHandler implements PlayMovieDetectionCallback {
    public PlayMovieDebugHandler(@NonNull DebugActivity activity) {
        super(activity, "bruh_2", true);
    }

    public void initMatch(Side servingSide) {
        super.initMatch(servingSide, MatchType.BO5);
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
