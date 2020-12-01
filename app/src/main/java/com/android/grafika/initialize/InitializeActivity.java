package com.android.grafika.initialize;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.grafika.CameraPreviewActivity;
import com.otaliastudios.zoom.ZoomLayout;

import cz.fmo.R;
import cz.fmo.display.MatchSelectCornerFragment;

/**
 * Activity to initialize a table tennis game
 * I.e. Select Table corners, settings, ...
 */
public final class InitializeActivity extends CameraPreviewActivity {
    private static final String TAG_SELECT_CORNERS = "SELECT_CORNERS";
    private static final String TAG_SPECIFY_MATCH = "SPECIFY_MATCH";
    private static final String TAG_DONE = "DONE";
    private static final String TAG_CREATE_MATCH_ID = "MATCH_ID";
    private SurfaceView tableSurface;
    private SurfaceHolder.Callback tableSurfaceCallback;
    private ZoomLayout zoomLayout;
    private MatchSelectCornerFragment initSelectCornerFragment;
    private final Point[] tableCorners = new Point[4];
    private int currentCornerIndex;
    private int selectedMatchType;
    private int selectedServingSide;
    private String selectedMatchId;

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
        setInitializeSelectingCornersFragment();
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
        setContentView(R.layout.fragment_match_corners);
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
     * Turn on the camera if we went back to the last fragment which needs the camera preview.
     */
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Fragment f = getFragmentManager().findFragmentByTag(TAG_SELECT_CORNERS);
        if (f instanceof MatchSelectCornerFragment) {
            this.onResume();
        }
    }

    public void startBackgroundAnimation() {
        RelativeLayout relativeLayout = findViewById(R.id.mainBackground);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
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
        this.onPause();
        setInitializeCreateMatchRoomFragment();
    }

    void onMatchIDSelected(String matchId) {
        this.selectedMatchId = matchId;
        setInitializeDoneFragment();
    }

    String getMatchID() {
        return this.selectedMatchId.toLowerCase();
    }

    private void switchFragment(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //transaction.replace(R.id.init_fragmentPlaceholder, fragment, tag);
        if(addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setInitializeSelectingCornersFragment() {
        this.initSelectCornerFragment = InitializeFragmentFactory.newSelectingCornersInstance();
        switchFragment(this.initSelectCornerFragment, TAG_SELECT_CORNERS,false);
    }

    private void setInitializeSpecifyMatchFragment() {
        switchFragment(InitializeFragmentFactory.newSpecifyMatchInstance(), TAG_SPECIFY_MATCH,true);
    }

    private void setInitializeDoneFragment() {
        this.initSelectCornerFragment = InitializeFragmentFactory.newDoneInstance();
        switchFragment(this.initSelectCornerFragment, TAG_DONE, true);
    }

    private void setInitializeCreateMatchRoomFragment() {
        switchFragment(InitializeFragmentFactory.newCreateRoomInstance(), TAG_CREATE_MATCH_ID, true);
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
                    setInitializeSpecifyMatchFragment();
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
                    setInitializeSelectingCornersFragment();
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
