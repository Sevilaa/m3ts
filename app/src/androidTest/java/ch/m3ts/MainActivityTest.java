package ch.m3ts;

import android.Manifest;
import android.app.Activity;
import android.support.test.uiautomator.UiDevice;

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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.anything;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    private static final String MATCH_SCORE_TAG = "MATCH_SCORE";

    @Rule
    public GrantPermissionRule grantPermissionRuleCamera = GrantPermissionRule.grant(android.Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);

    @Rule
    public ActivityTestRule<MainActivity> pmsMainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void grantAllPermissions() {
        GrantPermission.grantAllPermissions();
    }

    @Test
    // plays a video for a couple of seconds (with bounces in it), and then checks if there was a bounce
    public void testGoToMatchAndInitActivity() {
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        Activity activity = pmsMainActivityRule.getActivity();
        onView(withId(R.id.mainUseAsTrackerBtn))
                .check(matches(isDisplayed()));
        onView(withId(R.id.mainUseAsDisplayBtn))
                .check(matches(isDisplayed()));
        onView(withId(R.id.mainUseAsDisplayBtn))
                .perform(click());

        // check if we are at the MatchActivity
        onView(withId(R.id.match_type_bo1))
                .check(matches(isDisplayed()));
        onView(withId(R.id.right_side_server_icon))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_sideAndMatchTypeDoneBtn))
                .check(matches(isDisplayed()));

        // test the fragments of MatchActivity
        checkMatchSettingsAndQRCode();
        mDevice.pressBack();
        mDevice.pressBack();

        // should be back now
        onView(withId(R.id.mainUseAsDisplayBtn))
                .check(matches(isDisplayed()));
        onView(withId(R.id.mainUseAsTrackerBtn))
                .perform(click());
        GrantPermission.grantAllPermissions();

        // should be in init activity
        onView(withId(R.id.scan_overlay))
                .check(matches(isDisplayed()));
        mDevice.pressBack();
        activity.finish();
    }

    private void checkMatchSettingsAndQRCode() {
        // check if all components are there
        onView(withId(R.id.match_type_bo3))
                .check(matches(isDisplayed()));
        onView(withId(R.id.right_side_server_icon))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_sideAndMatchTypeDoneBtn))
                .check(matches(isDisplayed()));

        // perform some match setting selections...
        onView(withId(R.id.match_type_bo3))
                .perform(click());
        onView(withId(R.id.right_side_server_icon))
                .perform(click());
        onView(withId(R.id.init_sideAndMatchTypeDoneBtn))
                .perform(click());


        // now we should be greeted by a QR code
        onView(withId(R.id.qr_code))
                .check(matches(isDisplayed()));
    }
}