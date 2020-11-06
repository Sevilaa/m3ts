package com.android.grafika.initialize;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.grafika.CameraPreviewActivity;
import com.otaliastudios.zoom.ZoomLayout;

import cz.fmo.R;
import cz.fmo.SettingsActivity;

/**
 * Activity to initialize a table tennis game
 * I.e. Select Table corners, settings, ...
 */
public final class InitializeActivity extends CameraPreviewActivity {
    private SurfaceView tableSurface;
    private SurfaceHolder.Callback tableSurfaceCallback;
    private ZoomLayout zoomLayout;
    private InitializeSelectingCornersFragment initSelectCornerFragment;
    private final Point[] tableCorners = new Point[4];
    private int currentCornerIndex;
    private int selectedMatchType;
    private int selectedServingSide;

    @Override
    protected void onCreate(android.os.Bundle savedBundle) {
        super.onCreate(savedBundle);
        cameraCallback = new InitializeHandler(this);
        this.tableSurfaceCallback = new TableSurfaceCallback();
        this.tableSurface = findViewById(R.id.init_tableSurface);
        this.tableSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
        this.tableSurface.getHolder().addCallback(this.tableSurfaceCallback);
        this.zoomLayout = findViewById(R.id.init_zoomLayout);
        this.currentCornerIndex = 0;
        setInitializeSelectingFragment();
        setupOnLongTouchListener();
        setupOnRevertClickListener();
    }

    public boolean isTableDrawReady() {
        return ((TableSurfaceCallback) this.tableSurfaceCallback).isReady();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
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

    ZoomLayout getZoomLayout() {
        return zoomLayout;
    }

    int getCurrentCornerIndex() {
        return currentCornerIndex;
    }

    Point[] getTableCorners() {
        return tableCorners;
    }

    SurfaceView getTableSurface() {
        return tableSurface;
    }

    int getSelectedServingSide() {
        return selectedServingSide;
    }

    void setSelectedServingSide(int selectedServingSide) {
        this.selectedServingSide = selectedServingSide;
    }

    int getSelectedMatchType() {
        return selectedMatchType;
    }

    void setSelectedMatchType(int selectedMatchType) {
        this.selectedMatchType = selectedMatchType;
    }

    void onSideAndMatchSelectDone() {
        setDoneFragment();
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.init_fragmentPlaceholder, fragment);
        transaction.commit();
    }

    private void setInitializeSelectingFragment() {
        this.initSelectCornerFragment = InitializeSelectingCornersFragment.newInstance(this);
        switchFragment(this.initSelectCornerFragment);
    }

    private void setInitializeMatchTypeAndServerFragment() {
        switchFragment(InitializeSelectingGameFragment.newInstance(this));
    }

    private void setDoneFragment() {
        this.initSelectCornerFragment = InitializeDoneSelectingCornersFragment.newInstance(this);
        switchFragment(this.initSelectCornerFragment);
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
                currentCornerIndex++;
                initSelectCornerFragment.onStateChanged();
                initSelectCornerFragment.updateViews();
                if (currentCornerIndex == tableCorners.length) {
                    setInitializeMatchTypeAndServerFragment();
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
                initSelectCornerFragment.onStateChanged();
                initSelectCornerFragment.updateViews();
            }
        });
    }

    private Point makeAbsPoint(float x, float y, float zoom, float panX, float panY) {
        float absX = x/zoom + Math.abs(panX);
        float absY = y/zoom + Math.abs(panY);
        return new Point(Math.round(absX), Math.round(absY));
    }

    private static class TableSurfaceCallback implements SurfaceHolder.Callback {
        private boolean isReady;

        TableSurfaceCallback() {
            this.isReady = false;
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            this.isReady = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            // no implementation needed
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            // no implementation needed
        }

        public boolean isReady() {
            return isReady;
        }
    }
}
