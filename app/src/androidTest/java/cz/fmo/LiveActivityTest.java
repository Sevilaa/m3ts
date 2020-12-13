package cz.fmo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ImageButton;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import ch.m3ts.tracker.visualization.live.LiveActivity;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LiveActivityTest {
    private LiveActivity liveDebugActivity;
    private ImageButton settingsButton;
    private GridLayout debugGridLayout;

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
            bundle.putIntArray("CORNERS_UNSORTED", new int[] {0,0,0,0,0,0,0,0});
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
        settingsButton = liveDebugActivity.findViewById(R.id.live_settings_button);
        debugGridLayout = liveDebugActivity.findViewById(R.id.playMovie_debugGrid);
    }

    @After
    public void tearDown() {
        liveDebugActivity.finish();
        liveDebugActivity = null;
    }

    @Test
    public void goToSettings() {
        findAllViews();
        onView(withId(R.id.live_settings_button))
                .perform(click());

        // check if we switched to settings by checking if some views are displayed
        onView(withText(R.string.prefHeaderCapture))
                .check(matches(isDisplayed()));
        onView(withText(R.string.prefHeaderDetection))
                .check(matches(isDisplayed()));
        onView(withText(R.string.prefHeaderVelocity))
                .check(matches(isDisplayed()));
        onView(withText(R.string.prefHeaderAdvanced))
                .check(matches(isDisplayed()));
    }

    private void findAllViews() {
        assertNotNull(settingsButton);
        assertNotNull(debugGridLayout);
    }
}