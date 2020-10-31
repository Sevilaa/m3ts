package com.android.grafika;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;

import cz.fmo.R;
import cz.fmo.SettingsActivity;
import cz.fmo.tabletennis.Table;
import cz.fmo.util.Config;

/**
 * The main activity, facilitating video preview, encoding and saving.
 */
public final class LiveDebugActivity extends DebugActivity {
    private static final String CORNERS_PARAM = "CORNERS_UNSORTED";
    private Config mConfig;
    private LiveDebugHandler mHandler;

    @Override
    protected void onCreate(android.os.Bundle savedBundle) {
        super.onCreate(savedBundle);
        this.mHandler = new LiveDebugHandler(this);
        cameraCallback = this.mHandler;
        this.mConfig = new Config(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void init() {
        super.init();
        if (!mConfig.isDisableDetection() && ismSurfaceHolderReady()) {
            // C++ initialization
            mHandler.init(mConfig, this.getCameraWidth(), this.getCameraHeight());
            trySettingTableLocationFromIntent();
            mHandler.startDetections();
        }
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

    /**
     * Perform cleanup after the activity has been paused.
     */
    @Override
    protected void onPause() {
        mHandler.stopDetections();
        super.onPause();
    }

    private void trySettingTableLocationFromIntent() {
        int[] cornerInts = getCornerIntArrayFromIntent();
        scaleCornerIntsToSelectedCamera(cornerInts);
        mHandler.setTable(Table.makeTableFromIntArray(cornerInts));
    }

    private int[] getCornerIntArrayFromIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            throw new UnableToGetBundleException();
        }
        int[] cornerIntArray = bundle.getIntArray(CORNERS_PARAM);
        if (cornerIntArray == null) {
            throw new NoCornersInIntendFoundException();
        }
        return cornerIntArray;
    }

    private void scaleCornerIntsToSelectedCamera(int[] cornerInts) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float xScale = (float) this.getCameraWidth() / size.x;
        float yScale = (float) this.getCameraHeight() / size.y;
        for(int i = 0; i<cornerInts.length; i++) {
            if(i%2 == 0) {
                cornerInts[i] = Math.round(cornerInts[i] * xScale);
            } else {
                cornerInts[i] = Math.round(cornerInts[i] * yScale);
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
