package com.android.grafika.tracker;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.grafika.CameraPreviewActivity;
import com.android.grafika.LiveDebugActivity;
import com.android.grafika.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import cz.fmo.R;
import cz.fmo.TrackerPubNub;

/**
 * Activity to initialize a table tennis game
 * I.e. Select Table corners, settings, ...
 */
public final class InitTrackerActivity extends CameraPreviewActivity {
    private static final String CORNERS_PARAM = "CORNERS_UNSORTED";
    private static final String MATCH_TYPE_PARAM = "MATCH_TYPE";
    private static final String SERVING_SIDE_PARAM = "SERVING_SIDE";
    private static final String MATCH_ID = "MATCH_ID";
    private TrackerPubNub trackerPubNub;

    @Override
    protected void onCreate(android.os.Bundle savedBundle) {
        super.onCreate(savedBundle);
        cameraCallback = new InitTrackerHandler(this);
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

    void createPubNubRoom(String matchId) {
        try {
            Properties properties = new Properties();
            try (InputStream is = this.getAssets().open("app.properties")) {
                properties.load(is);
                this.trackerPubNub = new TrackerPubNub(matchId, properties.getProperty("pub_key"), properties.getProperty("sub_key"));
                this.trackerPubNub.setInitTrackerCallback((InitTrackerCallback) this.cameraCallback);
            }
        } catch (IOException ex) {
            Log.d("No properties file found, using display of this device...");
        }
    }

    void switchToDebugActivity(String selectedMatchId, int selectedMatchType, int selectedServingSide, int[] tableCorners) {
        this.trackerPubNub.unsubscribe();
        Intent intent = new Intent(this, LiveDebugActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(MATCH_ID, selectedMatchId);
        bundle.putInt(MATCH_TYPE_PARAM, selectedMatchType);
        bundle.putInt(SERVING_SIDE_PARAM, selectedServingSide);
        bundle.putIntArray(CORNERS_PARAM, tableCorners);
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }

    private static int[] pointsToIntArray(Point[] points) {
        int[] ints = new int[points.length*2];
        for(int i = 0; i<points.length; i++) {
            Point point = points[i];
            ints[i*2] = point.x;
            ints[i*2+1] = point.y;
        }
        return ints;
    }
}
