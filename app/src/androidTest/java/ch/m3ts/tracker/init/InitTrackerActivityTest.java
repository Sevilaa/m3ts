package ch.m3ts.tracker.init;

import android.app.Activity;
import android.content.Intent;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.MatchType;
import ch.m3ts.tracker.visualization.CameraPreviewActivity;
import ch.m3ts.tracker.visualization.live.LiveActivity;
import cz.fmo.R;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class InitTrackerActivityTest extends InstrumentationTestCase {
    private InitTrackerActivity activity;
    private String QR_CODE_PATH = "yuvimg.yuv";
    private String SCAN_OVERLAY_TEXT = "Scan the QR code from the display";
    private String WAIT_FOR_PICTURE_TEXT = "Sending Frame...";

    @Rule
    public ActivityTestRule<InitTrackerActivity> initActivityRule = new ActivityTestRule<InitTrackerActivity>(InitTrackerActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            return new Intent(Intent.ACTION_MAIN);
        }
    };

    @Before
    public void setUp() {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        activity = initActivityRule.getActivity();
        GrantPermission.grantAllPermissions();
    }

    @After
    public void tearDown() {
        activity.finish();
        activity = null;
    }

    @Test
    public void scanOverlayDisplayed() {
        onView(withId(R.id.scan_overlay)).check(matches(isDisplayed()));
        onView(withText(SCAN_OVERLAY_TEXT)).check(matches(isDisplayed()));
    }

    @Test
    public void waitForPictureLayoutDisplayed() throws IllegalAccessException, NoSuchFieldException, IOException {
        Field cameraCallbackField = CameraPreviewActivity.class.getDeclaredField("cameraCallback");
        cameraCallbackField.setAccessible(true);
        InitTrackerHandler cameraCallback = (InitTrackerHandler) cameraCallbackField.get(activity);
        cameraCallback.onCameraFrame(loadQRCodeBytes());
        onView(withId(R.id.scan_overlay)).check(matches(not(isDisplayed())));
        cameraCallback.onCaptureFrame();
        onView(withId(R.id.tracker_loading)).check(matches(isDisplayed()));
        onView(withId(R.id.tracker_info)).check(matches(isDisplayed()));
        onView(withId(R.id.loading_bar)).check(matches(isDisplayed()));
    }

    @Test
    public void switchToLiveActivity() throws Throwable{
        String matchID = "some_id";
        int selectedMatchType = 0;
        int selectedServingSide = 1;
        int[] tableCorners = new int[12];
        // some table corner points (with net)
        tableCorners[0] = 5;
        tableCorners[1] = 60;
        tableCorners[2] = 95;
        tableCorners[3] = 61;
        tableCorners[4] = 90;
        tableCorners[5] = 53;
        tableCorners[6] = 11;
        tableCorners[7] = 54;
        tableCorners[8] = 52;
        tableCorners[9] = 60;
        tableCorners[10] = 50;
        tableCorners[11] = 53;
        activity.switchToLiveActivity(matchID, selectedMatchType, selectedServingSide, tableCorners);
        // should now be in the live activity
        onView(withId(R.id.playMovie_surfaceTracks)).check(matches(isDisplayed()));
        onView(withId(R.id.playMovie_surfaceTable)).check(matches(isDisplayed()));
        Activity activity = getCurrentActivity();
        assertTrue(getCurrentActivity() instanceof LiveActivity);
        LiveActivity liveActivity = (LiveActivity) activity;

        // assert table corners
        Field liveActivityField = LiveActivity.class.getDeclaredField("tableCorners");
        liveActivityField.setAccessible(true);
        int[] tableCornersInLive = (int[]) liveActivityField.get(liveActivity);
        assertEquals(tableCornersInLive.length, tableCorners.length);
        for (int i = 0; i<tableCornersInLive.length; i++) {
            // tableCorners should be formatted to abs values
            assertNotEquals(tableCornersInLive[i], tableCorners[i]);
        }

        // assert matchType
        liveActivityField = LiveActivity.class.getDeclaredField("matchType");
        liveActivityField.setAccessible(true);
        MatchType matchTypeInLive = (MatchType) liveActivityField.get(liveActivity);
        assertEquals(matchTypeInLive, MatchType.values()[selectedMatchType]);

        // assert servingSide
        liveActivityField = LiveActivity.class.getDeclaredField("servingSide");
        liveActivityField.setAccessible(true);
        Side servingSideInLive = (Side) liveActivityField.get(liveActivity);
        assertEquals(servingSideInLive, Side.values()[selectedServingSide]);
    }

    private byte[] loadQRCodeBytes() throws IOException {
        InputStream inputStream = activity.getAssets().open(QR_CODE_PATH);
        byte[] fileBytes = new byte[inputStream.available()];
        inputStream.read(fileBytes);
        inputStream.close();
        return fileBytes;
    }

    Activity getCurrentActivity() throws Throwable {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                java.util.Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                activity[0] = Iterables.getOnlyElement(activities);
            }});
        return activity[0];
    }
}
