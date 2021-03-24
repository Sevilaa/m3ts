package ch.m3ts.tracker.visualization.live;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.SurfaceView;

import ch.m3ts.Log;
import ch.m3ts.helper.QuitAlertDialogHelper;
import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tabletennis.match.Player;
import ch.m3ts.tracker.visualization.CameraStatus;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import cz.fmo.R;
import cz.fmo.camera.CameraThread;
import cz.fmo.camera.PreviewCameraTarget;
import cz.fmo.data.Assets;
import cz.fmo.recording.EncodeThread;
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
    private LiveHandler mHandler;
    private int[] tableCorners;
    private MatchType matchType;
    private Side servingSide;
    private String matchId;
    private AlertDialog alertDialog;
    private LiveRecording liveRecording;

    @Override
    protected void onCreate(android.os.Bundle savedBundle) {
        super.onCreate(savedBundle);
        Config mConfig = new Config(this);
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
    protected void onStop() {
        super.onStop();
        this.mHandler.setConnectCallback(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isAudioPermissionDenied()) {
            String[] perms = new String[]{Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(this, perms, 0);
        }
    }

    @Override
    public void init() {
        // reality check: don't initialize twice
        if (mStatus == CameraStatus.RUNNING) return;

        if (!ismSurfaceHolderReady()) return;

        // stop if permissions haven't been granted
        if (isCameraPermissionDenied()) {
            mStatus = CameraStatus.CAMERA_PERMISSION_ERROR;
            return;
        }

        // get configuration settings
        Config mConfig = new Config(this);

        // set up assets
        Assets.getInstance().load(this);

        // create a dedicated camera input thread
        mCamera = new CameraThread(this.cameraCallback, mConfig);
        this.cameraHorizontalAngle = mCamera.getCameraHorizontalViewAngle();

        // add preview as camera target
        SurfaceView cameraView = getmSurfaceView();
        PreviewCameraTarget mPreviewTarget = new PreviewCameraTarget(cameraView.getHolder().getSurface(),
                cameraView.getWidth(), cameraView.getHeight());
        mCamera.addTarget(mPreviewTarget);

        if (mConfig.isSlowPreview()) {
            mPreviewTarget.setSlowdown(PREVIEW_SLOWDOWN_FRAMES);
        }

        // refresh GUI
        mStatus = CameraStatus.RUNNING;

        if(mConfig.doRecordMatches()) {
            if (liveRecording != null) liveRecording.tearDown();
            liveRecording = LiveRecording.getInstance(this, mCamera);
            liveRecording.startRecording();
        }

        // start thread
        mCamera.start();
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

    @Override
    protected void onResume() {
        super.onResume();
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
        if (liveRecording != null) liveRecording.tearDown();
    }

    @Override
    public void onBackPressed() {
        alertDialog.show();
    }

    public EncodeThread getmEncode() {
        if (liveRecording != null) return liveRecording.getmEncode();
        return null;
    }

    private boolean isAudioPermissionDenied() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return permissionStatus != PackageManager.PERMISSION_GRANTED;
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
