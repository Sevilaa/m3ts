package com.android.grafika.initialize;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.grafika.CameraPreviewActivity;
import com.android.grafika.Log;
import com.otaliastudios.zoom.ZoomLayout;

import cz.fmo.R;
import cz.fmo.SettingsActivity;
import cz.fmo.camera.CameraThread;
import cz.fmo.camera.PreviewCameraTarget;
import cz.fmo.data.Assets;
import cz.fmo.data.TrackSet;
import cz.fmo.util.Config;

/**
 * Activity to initialize a table tennis game
 * I.e. Select Table corners, settings, ...
 */
public final class InitializeActivity extends CameraPreviewActivity {
    private static final int PREVIEW_SLOWDOWN_FRAMES = 59;
    private Status mStatus = Status.STOPPED;
    private CameraThread mCamera;
    private SurfaceView tableSurface;
    private ZoomLayout zoomLayout;
    private InitializeSelectingCornersFragment initSelectCornerFragment;
    private final Point[] tableCorners = new Point[4];
    private int currentCornerIndex;

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
            return;
        }

        // get configuration settings
        Config mConfig = new Config(this);

        // set up assets
        Assets.getInstance().load(this);

        // create a dedicated camera input thread
        mCamera = new CameraThread(new InitializeHandler(this), mConfig);

        // add preview as camera target
        SurfaceView cameraView = getmSurfaceView();
        PreviewCameraTarget mPreviewTarget = new PreviewCameraTarget(cameraView.getHolder().getSurface(),
                cameraView.getWidth(), cameraView.getHeight());
        mCamera.addTarget(mPreviewTarget);

        if (mConfig.isSlowPreview()) {
            mPreviewTarget.setSlowdown(PREVIEW_SLOWDOWN_FRAMES);
        }

        // refresh GUI
        mStatus = Status.RUNNING;

        // start thread
        mCamera.start();
    }

    @Override
    public void setCurrentContentView() {
        setContentView(R.layout.activity_initialize);
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

    public ZoomLayout getZoomLayout() {
        return zoomLayout;
    }

    public Point[] getTableCorners() {
        return tableCorners;
    }

    public SurfaceView getTableSurface() {
        return tableSurface;
    }

    @Override
    protected void onCreate(android.os.Bundle savedBundle) {
        super.onCreate(savedBundle);
        this.tableSurface = findViewById(R.id.init_tableSurface);
        this.tableSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
        this.zoomLayout = findViewById(R.id.init_zoomLayout);
        this.currentCornerIndex = 0;
        setInitializeSelectingFragment();
        setupOnLongTouchListener();
        setupOnRevertClickListener();
    }

    private void setInitializeSelectingFragment() {
        this.initSelectCornerFragment = InitializeSelectingCornersFragment.newInstance(String.valueOf(this.currentCornerIndex),
                String.valueOf(this.tableCorners.length));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.init_fragmentPlaceholder, this.initSelectCornerFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setDoneFragment() {
        this.initSelectCornerFragment = InitializeDoneSelectingCornersFragment.newInstance(String.valueOf(this.currentCornerIndex),
                String.valueOf(this.tableCorners.length), this.tableCorners);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.init_fragmentPlaceholder, this.initSelectCornerFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupOnLongTouchListener() {
        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                if (currentCornerIndex >= tableCorners.length) return;
                float x = e.getX();
                float y = e.getY();
                float zoom = zoomLayout.getZoom();
                float panX = zoomLayout.getPanX();
                float panY = zoomLayout.getPanY();
                Point absPoint = makeAbsPoint(x,y,zoom,panX,panY);
                tableCorners[currentCornerIndex] = absPoint;
                initSelectCornerFragment.setSelectedCornersText(currentCornerIndex+1);
                Log.d("x:"+absPoint.x+" y:"+absPoint.y);
                Log.d("Longpress detected");
                currentCornerIndex++;
                if (currentCornerIndex == tableCorners.length) {
                    setDoneFragment();
                }
            }
        });

        this.zoomLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
    }

    private void setupOnRevertClickListener() {
        findViewById(R.id.init_revertButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCornerIndex <= 0) return;
                currentCornerIndex--;
                tableCorners[currentCornerIndex] = null;
                if (currentCornerIndex == tableCorners.length-1) {
                    setInitializeSelectingFragment();
                    return;
                }
                initSelectCornerFragment.setSelectedCornersText(currentCornerIndex);
            }
        });
    }



    private Point makeAbsPoint(float x, float y, float zoom, float panX, float panY) {
        float absX = x/zoom + Math.abs(panX);
        float absY = y/zoom + Math.abs(panY);
        return new Point(Math.round(absX), Math.round(absY));
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
        FrameLayout layout = findViewById(R.id.frameLayout);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        params.height = size.y;
        params.width = size.x;
        layout.setLayoutParams(params);
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

        TrackSet.getInstance().clear();

        mStatus = Status.STOPPED;
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
