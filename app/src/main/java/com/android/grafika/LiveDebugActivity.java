package com.android.grafika;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import cz.fmo.R;
import cz.fmo.SettingsActivity;
import cz.fmo.camera.CameraThread;
import cz.fmo.camera.PreviewCameraTarget;
import cz.fmo.data.Assets;
import cz.fmo.data.TrackSet;
import cz.fmo.recording.EncodeThread;
import cz.fmo.tabletennis.Table;
import cz.fmo.util.Config;

/**
 * The main activity, facilitating video preview, encoding and saving.
 */
public final class LiveDebugActivity extends DebugActivity {
    private static final int PREVIEW_SLOWDOWN_FRAMES = 59;
    private Config mConfig;
    private Status mStatus = Status.STOPPED;
    private CameraThread mCamera;
    private EncodeThread mEncode;
    private PreviewCameraTarget mPreviewTarget;
    private LiveDebugHandler mHandler;
    private TextView mStatusText;
    private String mStatusTextLast;

    /**
     * The main initialization step. There are multiple callers of this method, but mechanisms are
     * put into place so that the actual initialization happens exactly once. There is no need for
     * a mutex, assuming that all entry points are run by the main thread.
     * <p>
     * Called by:
     * - onResume()
     * - GUI.surfaceCreated(), when GUI preview surface has just been created
     * - onRequestPermissionsResult(), when camera permissions have just been granted
     */
    @Override
    public void init() {
        // reality check: don't initialize twice
        if (mStatus == Status.RUNNING) return;

        if (!ismSurfaceHolderReady()) return;

        // stop if permissions haven't been granted
        if (isCameraPermissionDenied()) {
            mStatus = Status.CAMERA_PERMISSION_ERROR;
            updateStatusString();
            return;
        }

        // get configuration settings
        mConfig = new Config(this);

        // set up assets
        Assets.getInstance().load(this);

        // set up track set
        TrackSet.getInstance().setConfig(mConfig);

        // create a dedicated camera input thread
        mCamera = new CameraThread(mHandler, mConfig);

        // add preview as camera target
        SurfaceView cameraView = getmSurfaceView();
        mPreviewTarget = new PreviewCameraTarget(cameraView.getHolder().getSurface(),
                cameraView.getWidth(), cameraView.getHeight());
        mCamera.addTarget(mPreviewTarget);

        if (mConfig.isSlowPreview()) {
            mPreviewTarget.setSlowdown(PREVIEW_SLOWDOWN_FRAMES);
        }

        if (!mConfig.isDisableDetection()) {
            // C++ initialization
            Config mConfig = new Config(this);
            mHandler.init(mConfig, mCamera.getWidth(), mCamera.getHeight());
            trySettingTableLocationFromXML("outdoor_good");
            mHandler.startDetections();
        }

        // refresh GUI
        mStatus = Status.RUNNING;
        updateStatusString();

        // start threads
        if (mEncode != null) mEncode.start();
        mCamera.start();
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

    public void onOpenMenu(View toggle) {
        setmSurfaceHolderReady(false);
        startActivity(new Intent(this, SettingsActivity.class));
    }


    public EncodeThread getmEncode() {
        return mEncode;
    }

    @Override
    protected void onCreate(android.os.Bundle savedBundle) {
        super.onCreate(savedBundle);
        mStatusText = findViewById(R.id.camera_status);
        mStatusTextLast = null;
        this.mHandler = new LiveDebugHandler(this);
    }

    /**
     * Responsible for querying and acquiring camera permissions. Whatever the response will be,
     * the permission request could result in the application being paused and resumed. For that
     * reason, requesting permissions at any later point, including in onResume(), might cause an
     * infinite loop.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (isCameraPermissionDenied()) {
            String[] perms = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, perms, 0);
        }
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
        super.onPause();

        mHandler.stopDetections();

        if (mCamera != null) {
            mCamera.getHandler().sendKill();
            try {
                mCamera.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                com.android.grafika.Log.e("Interrupted when closing CameraThread", ie);
            }
            mCamera = null;
        }

        if (mEncode != null) {
            mEncode.getHandler().sendKill();
            try {
                mEncode.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                com.android.grafika.Log.e("Interrupted when closing EncodeThread", ie);
            }
            mEncode = null;
        }

        TrackSet.getInstance().clear();

        mStatus = Status.STOPPED;
    }

    private void updateStatusString() {
        if(mStatus != Status.STOPPED) {
            String text;
            if (mStatus == Status.CAMERA_ERROR) {
                text = getString(R.string.errorCamera);
            } else if (mStatus == Status.CAMERA_PERMISSION_ERROR) {
                text = getString(R.string.errorPermissionCamera);
            } else {
                text = "";
            }

            if (mStatusTextLast != null && !mStatusTextLast.equals(text)) {
                mStatusText.setText(text);
                mStatusTextLast = text;
            }
        }
    }


    // TODO delete when coordinate picker is implemented
    private void trySettingTableLocationFromXML(String videoFileName) {
        String fileNameWithoutExtension = videoFileName.split("\\.")[0];
        try (InputStream is = getAssets().open(fileNameWithoutExtension+".xml")) {
            Properties properties = new Properties();
            properties.loadFromXML(is);
            mHandler.setTable(Table.makeTableFromProperties(properties));
            Log.d("found new table!");
        } catch (IOException ex) {
            Log.e(ex.getMessage(), ex);
        }
    }

    /**
     * Queries the camera permission status.
     */
    private boolean isCameraPermissionDenied() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return permissionStatus != PackageManager.PERMISSION_GRANTED;
    }

    private enum Status {
        STOPPED, RUNNING, CAMERA_ERROR, CAMERA_PERMISSION_ERROR
    }
}
