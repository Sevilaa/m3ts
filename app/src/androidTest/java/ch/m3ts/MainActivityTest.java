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
import cz.fmo.util.Config;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;

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

        // check for alertDialog
        pressBack();
        onView(withText(R.string.quitMatchMessage)).check(matches(isDisplayed()));
        onView(withText(R.string.quitMatchProceed)).perform(click());
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
        onView(withText(activity.getString(R.string.quitMatchProceed))).perform(click());

        // should be back in main activity
        onView(withId(R.id.mainUseAsDisplayBtn))
                .check(matches(isDisplayed()));
        onView(withId(R.id.mainUseAsTrackerBtn))
                .perform(click());
        GrantPermission.grantAllPermissions();

        // should be in init activity
        onView(withId(R.id.adjust_device_overlay))
                .check(matches(isDisplayed()));
        mDevice.pressBack();
        activity.finish();
    }

    @Test
    public void checkAppSettings() {
        Activity activity = pmsMainActivityRule.getActivity();
        Config config = new Config(activity);
        String newPlayer1Name = "Some Player 1";
        String newPlayer2Name = "My other Player";
        onView(withId(R.id.live_settings_button)).perform(click());
        onView(withText(R.string.prefPlayer1Name)).perform(click());
        onView(allOf(withClassName(endsWith("EditText"))))
                .perform(replaceText(newPlayer1Name));
        onView(withText("OK")).perform(click());
        onView(withText(R.string.prefPlayer2Name)).perform(click());
        onView(allOf(withClassName(endsWith("EditText"))))
                .perform(replaceText(newPlayer2Name));
        onView(withText("OK")).perform(click());

        boolean isUseDebug = config.isUseDebug();
        boolean recordMatches = config.doRecordMatches();
        onView(withText(R.string.prefDisplayDebug)).perform(click());
        onView(withText(R.string.prefRecord)).perform(click());

        // now check the edited settings in main activity
        pressBack();
        config = new Config(activity);
        assertEquals(!isUseDebug, config.isUseDebug());
        assertEquals(!recordMatches, config.doRecordMatches());
        assertEquals(newPlayer1Name, config.getPlayer1Name());
        assertEquals(newPlayer2Name, config.getPlayer2Name());

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

        Activity activity = pmsMainActivityRule.getActivity();
        Config config = new Config(activity);

        if (config.isUsingPubnub()) {
            // now we should be greeted by a QR code
            onView(withId(R.id.qr_code))
                    .check(matches(isDisplayed()));
        } else {
            onView(withId(R.id.qr_code))
                    .check(matches(not(isDisplayed())));
            onView(withText(R.string.connectDisplaySearching))
                    .check(matches(isDisplayed()));
        }
    }
}