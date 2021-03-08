package ch.m3ts.tracker.visualization.live;

import android.app.AlertDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;

import ch.m3ts.Log;
import ch.m3ts.helper.QuitAlertDialogHelper;
import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tabletennis.match.Player;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import cz.fmo.R;
import cz.fmo.util.Config;

/**
 * Activity which reads the ball movement of the table tennis ball and updates the table tennis match
 * when relevant events occur.
 */
@SuppressWarnings("squid:S110")
public final class LiveActivity extends MatchVisualizeActivity {
    private static final String CORNERS_PARAM = "CORNERS_UNSORTED";
    private static final String MATCH_TYPE_PARAM = "MATCH_TYPE";
    private static final String SERVING_SIDE_PARAM = "SERVING_SIDE";
    private static final String MATCH_ID = "MATCH_ID";
    private Config mConfig;
    private LiveHandler mHandler;
    private int[] tableCorners;
    private MatchType matchType;
    private Side servingSide;
    private String matchId;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(android.os.Bundle savedBundle) {
        super.onCreate(savedBundle);
        this.mConfig = new Config(this);
        getDataFromIntent();
        Player playerLeft = new Player(mConfig.getPlayer1Name());
        Player playerRight = new Player(mConfig.getPlayer2Name());
        this.mHandler = new LiveHandler(this, this.matchId);
        this.mHandler.initMatch(this.servingSide, this.matchType, playerLeft, playerRight);
        cameraCallback = this.mHandler;
        this.alertDialog = QuitAlertDialogHelper.makeDialog(this);
        Log.d("Found match: " +matchId);
        this.onPause();
    }

    @Override
    public void init() {
        super.init();
        Table table = trySettingTableLocationFromIntent();
        mHandler.init(mConfig, this.getCameraWidth(), this.getCameraHeight(), table, this.getCameraHorizontalViewAngle());
        mHandler.startDetections();
    }

    @Override
    public void setCurrentContentView() {
        setContentView(R.layout.activity_live_debug);
    }

    /**
     * Called when a decision has been made regarding the camera permission. Whatever the response
     * is, the initialization procedure continues. If the permission is denied, the init() method
     * will display a proper error message on the screen.
     */
    @Override
    public void onRequestPermissionsResult(int requestID, @NonNull String[] permissionList,
                                           @NonNull int[] grantedList) {
        init();
    }

    /**
     * Perform cleanup after the activity has been paused.
     */
    @Override
    protected void onPause() {
        mHandler.stopDetections();
        alertDialog.dismiss();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        alertDialog.show();
    }

    private Table trySettingTableLocationFromIntent() {
        scaleCornerIntsToSelectedCamera();
        return Table.makeTableFromIntArray(tableCorners);
    }

    private void getDataFromIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            throw new UnableToGetBundleException();
        }
        tableCorners = bundle.getIntArray(CORNERS_PARAM);
        if (tableCorners == null) {
            throw new NoCornersInIntendFoundException();
        }
        formatRelPointsToAbsPoints(tableCorners);
        servingSide = Side.values()[bundle.getInt(SERVING_SIDE_PARAM)];
        matchType = MatchType.values()[bundle.getInt(MATCH_TYPE_PARAM)];
        matchId = bundle.getString(MATCH_ID);
    }

    private void formatRelPointsToAbsPoints(int[] points) {
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        for (int i = 0; i<points.length; i++) {
            if (i%2 == 0) {
                points[i] = (int) Math.round(points[i] / 100.0 * displaySize.x);
            } else {
                points[i] = (int) Math.round(points[i] / 100.0 * displaySize.y);
            }
        }
    }

    private void scaleCornerIntsToSelectedCamera() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float xScale = (float) this.getCameraWidth() / size.x;
        float yScale = (float) this.getCameraHeight() / size.y;
        for (int i = 0; i < tableCorners.length; i++) {
            if (i % 2 == 0) {
                tableCorners[i] = Math.round(tableCorners[i] * xScale);
            } else {
                tableCorners[i] = Math.round(tableCorners[i] * yScale);
            }
        }
    }

    static class NoCornersInIntendFoundException extends RuntimeException {
        private static final String MESSAGE = "No corners have been found in the intent's bundle!";
        NoCornersInIntendFoundException() {
            super(MESSAGE);
        }
    }

    static class UnableToGetBundleException extends RuntimeException {
        private static final String MESSAGE = "Unable to get the bundle from Intent!";
        UnableToGetBundleException() {
            super(MESSAGE);
        }
    }
}
