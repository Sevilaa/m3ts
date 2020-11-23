package com.android.grafika;

import android.support.annotation.NonNull;

import cz.fmo.Lib;
import cz.fmo.tabletennis.MatchType;
import cz.fmo.tabletennis.Player;
import cz.fmo.tabletennis.Side;

public class PlayMovieDebugHandler extends DebugHandler implements PlayMovieDetectionCallback {
    public PlayMovieDebugHandler(@NonNull DebugActivity activity) {
        super(activity, Side.LEFT, MatchType.BO5, "bruh_2", true, new Player("Hans"), new Player("Peter"));
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