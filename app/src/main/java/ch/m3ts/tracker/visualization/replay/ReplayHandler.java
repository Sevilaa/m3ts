package ch.m3ts.tracker.visualization.replay;

import android.support.annotation.NonNull;

import ch.m3ts.Log;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tabletennis.match.Player;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import ch.m3ts.tracker.visualization.MatchVisualizeHandler;
import cz.fmo.Lib;

/**
 * Renders the images received by a mp4 file (see ./lib/VideoPlayer) onto the screen and also passes them to FMO.
 * <p>
 * FMO then finds detections and tracks and forwards them to the EventDetector, which then calls
 * for events on this Handler.
 **/
public class ReplayHandler extends MatchVisualizeHandler implements ReplayDetectionCallback {
    public ReplayHandler(@NonNull MatchVisualizeActivity activity) {
        super(activity);
    }

    public void initMatch(Side servingSide) {
        super.initMatch(servingSide, MatchType.BO5, new Player("Hans"), new Player("Peter"));
        super.deactivateReadyToServeGesture();
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
