package ch.m3ts.tracker.visualization.live;

import android.support.annotation.NonNull;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tabletennis.match.Player;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import ch.m3ts.tracker.visualization.MatchVisualizeHandler;
import cz.fmo.Lib;
import cz.fmo.camera.CameraThread;

public class LiveHandler extends MatchVisualizeHandler implements CameraThread.Callback {
    private static final int CAMERA_ERROR = 2;

    public LiveHandler(@NonNull MatchVisualizeActivity activity, String matchID) {
        super(activity, matchID, false);
    }

    @Override
    public void onCameraRender() {
        // no implementation
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
