package ch.m3ts.tracker.init;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import ch.m3ts.pubnub.PubNubFactory;
import ch.m3ts.pubnub.TrackerPubNub;
import ch.m3ts.tracker.visualization.CameraPreviewActivity;
import ch.m3ts.tracker.visualization.live.LiveActivity;
import cz.fmo.R;

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
        this.trackerPubNub = PubNubFactory.createTrackerPubNub(this, matchId);
        this.trackerPubNub.setInitTrackerCallback((InitTrackerCallback) this.cameraCallback);
    }

    void switchToLiveActivity(String selectedMatchId, int selectedMatchType, int selectedServingSide, int[] tableCorners) {
        this.trackerPubNub.unsubscribe();
        Intent intent = new Intent(this, LiveActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(MATCH_ID, selectedMatchId);
        bundle.putInt(MATCH_TYPE_PARAM, selectedMatchType);
        bundle.putInt(SERVING_SIDE_PARAM, selectedServingSide);
        bundle.putIntArray(CORNERS_PARAM, tableCorners);
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }
}
