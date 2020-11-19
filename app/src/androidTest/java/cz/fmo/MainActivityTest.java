package com.android.grafika;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.test.uiautomator.UiDevice;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import cz.fmo.MainActivity;
import cz.fmo.R;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

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
        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        pmsMainActivityRule.getActivity();
        onView(withId(R.id.mainUseAsDisplayBtn))
                .perform(click());

        // check if we are at the MatchActivity
        onView(withId(R.id.pubnub_id))
                .check(matches(isDisplayed()));
        enterARoomAsDisplay();
        mDevice.pressBack();
        mDevice.pressBack();

        // should be back now
        onView(withId(R.id.mainUseAsDisplayBtn))
                .check(matches(isDisplayed()));
        onView(withId(R.id.mainUseAsTrackerBtn))
                .perform(click());
        GrantPermission.grantAllPermissions();

        // should be in init activity
        onView(withText(R.string.initMarkTableLabel))
                .check(matches(isDisplayed()));
        mDevice.pressBack();
    }

    private void enterARoomAsDisplay() {
        onView(withId(R.id.connect_to_match_lbl))
                .check(matches(isDisplayed()));
        onView(withId(R.id.pubnub_id))
                .perform(clearText(), typeText("test_room"), closeSoftKeyboard());
        onView(withId(R.id.join_btn))
                .perform(click());
        onView(withId(R.id.left_score))
                .check(matches(withText("0")));
        onView(withId(R.id.right_score))
                .check(matches(withText("0")));
        onView(withId(R.id.left_games))
                .check(matches(withText("0")));
        onView(withId(R.id.right_games))
                .check(matches(withText("0")));
    }

    private void setMobileDataEnabled(Context context, boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final ConnectivityManager conman = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
        connectivityManagerField.setAccessible(true);
        final Object connectivityManager = connectivityManagerField.get(conman);
        final Class connectivityManagerClass =  Class.forName(connectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
    }
}