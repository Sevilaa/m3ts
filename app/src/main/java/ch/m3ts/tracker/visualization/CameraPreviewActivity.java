package ch.m3ts.tracker.visualization;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import ch.m3ts.Log;
import cz.fmo.R;
import cz.fmo.camera.CameraThread;
import cz.fmo.camera.PreviewCameraTarget;
import cz.fmo.data.Assets;
import cz.fmo.util.Config;

/**
 * Base Activity which uses the camera to preview on a SurfaceView
 */
public abstract class CameraPreviewActivity extends Activity implements SurfaceHolder.Callback {
    protected static final int PREVIEW_SLOWDOWN_FRAMES = 59;
    private SurfaceView mSurfaceView;
    protected CameraStatus mStatus = CameraStatus.STOPPED;
    protected CameraThread mCamera;
    private boolean mSurfaceHolderReady = false;
    protected double cameraHorizontalAngle;
    protected CameraThread.Callback cameraCallback;

    /**
     * In this method the inherited class must set the content view.
     *  i.E. setContentView(R.layout.activity_play_movie_surface);
     */
    public abstract void setCurrentContentView();

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

        // start thread
        mCamera.start();
    }

    public double getCameraHorizontalViewAngle() {
        return this.cameraHorizontalAngle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.cameraCallback = new DummyCameraCallback();
        this.setCurrentContentView();
        mSurfaceView = findViewById(R.id.playMovie_surface);
        mSurfaceView.getHolder().addCallback(this);
    }

    /**
     * Perform cleanup after the activity has been paused.
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.getHandler().sendKill();
            try {
                mCamera.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                Log.e("Interrupted when closing CameraThread", ie);
            }
            mCamera = null;
        }
        mStatus = CameraStatus.STOPPED;
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
    public void surfaceCreated(SurfaceHolder holder) {
        // There's a short delay between the start of the activity and the initialization
        // of the SurfaceHolder that backs the SurfaceView.  We don't want to try to
        // send a video stream to the SurfaceView before it has initialized, so we disable
        // the "play" button until this callback fires.
        Log.d("surfaceCreated");
        mSurfaceHolderReady = true;
        init();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // ignore
        Log.d("surfaceChanged fmt=" + format + " size=" + width + "x" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // ignore
        Log.d("Surface destroyed");
    }

    public SurfaceView getmSurfaceView() {
        return mSurfaceView;
    }

    public boolean ismSurfaceHolderReady() {
        return mSurfaceHolderReady;
    }

    public void setmSurfaceHolderReady(boolean mSurfaceHolderReady) {
        this.mSurfaceHolderReady = mSurfaceHolderReady;
    }

    public int getCameraWidth() {
        return mCamera.getWidth();
    }

    public int getCameraHeight() {
        return mCamera.getHeight();
    }

    /**
     * Queries the camera permission status.
     */
    protected boolean isCameraPermissionDenied() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return permissionStatus != PackageManager.PERMISSION_GRANTED;
    }
}
