package ch.m3ts.tracker.visualization.live;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import cz.fmo.R;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LiveActivityTest {
    private LiveActivity liveDebugActivity;
    private static final String CORNERS_PARAM = "CORNERS_UNSORTED";
    private static final String MATCH_TYPE_PARAM = "MATCH_TYPE";
    private static final String SERVING_SIDE_PARAM = "SERVING_SIDE";
    private static final String MATCH_ID = "MATCH_ID";

    @Rule
    public GrantPermissionRule grantPermissionRuleCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);

    @Rule
    public ActivityTestRule<LiveActivity> recordingActivityActivityTestRule = new ActivityTestRule<LiveActivity>(LiveActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            // add data to intend as this activity is only called with a int array
            // (representing corner points of the table)
            Intent intent = new Intent(Intent.ACTION_MAIN);
            Bundle bundle = new Bundle();
            bundle.putIntArray(CORNERS_PARAM, new int[] {0,0,0,0,0,0,0,0});
            bundle.putInt(MATCH_TYPE_PARAM, 0);
            bundle.putInt(SERVING_SIDE_PARAM, 0);
            bundle.putString(MATCH_ID, "test");
            intent.putExtras(bundle);
            return intent;
        }
    };

    @Before
    public void grantAllPermissions() {
        GrantPermission.grantAllPermissions();
    }

    @Before
    public void setUp() {
        liveDebugActivity = recordingActivityActivityTestRule.getActivity();
    }

    @After
    public void tearDown() {
        liveDebugActivity.finish();
        liveDebugActivity = null;
    }

    @Test
    public void testFindAllViews() {
        onView(withId(R.id.debugScoreLayoutWrapper))
                .check(matches(isDisplayed()));
        onView(withId(R.id.playMovie_surfaceTracks))
                .check(matches(isDisplayed()));
        onView(withId(R.id.playMovie_surfaceTable))
                .check(matches(isDisplayed()));
    }
}