package ch.m3ts.tracker.visualization.live;

import android.support.annotation.NonNull;

import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import ch.m3ts.tracker.visualization.MatchVisualizeHandler;
import cz.fmo.Lib;
import cz.fmo.camera.CameraThread;

/**
 * Renders the images received by the camera API onto the screen and also passes them to FMO.
 *
 * FMO then finds detections and tracks and forwards them to the EventDetector, which then calls
 * for events on this Handler.
 **/
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
        if(isWaitingForGesture()) {
            setWaitingForGesture(!getServeDetector().isReadyToServe(dataYUV420SP));
        }
    }

    @Override
    public void onCameraError() {
        if (hasMessages(CAMERA_ERROR)) return;
        sendMessage(obtainMessage(CAMERA_ERROR));
    }
}
