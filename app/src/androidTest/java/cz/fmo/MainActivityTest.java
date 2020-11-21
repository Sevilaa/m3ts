package com.android.grafika;

import android.Manifest;
import android.app.Activity;
import android.support.test.uiautomator.UiDevice;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import cz.fmo.MainActivity;
import cz.fmo.R;
import cz.fmo.display.MatchActivity;
import cz.fmo.display.MatchScoreFragment;
import cz.fmo.tabletennis.Side;
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
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    private static final String MATCH_SCORE_TAG = "MATCH_SCORE";
    private Random random = new Random();

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
        pmsMainActivityRule.getActivity();
        onView(withId(R.id.mainUseAsDisplayBtn))
                .perform(click());

        // check if we are at the MatchActivity
        onView(withId(R.id.pubnub_id))
                .check(matches(isDisplayed()));
        enterARoomAsDisplay();

        // test the fragments of MatchActivity
        Activity activity = getActivityInstance();
        assertTrue(getActivityInstance() instanceof MatchActivity);
        MatchActivity matchActivity = (MatchActivity) activity;
        MatchScoreFragment fragment = (MatchScoreFragment) matchActivity.getFragmentManager().findFragmentByTag(MATCH_SCORE_TAG);
        testMatchScoreFragment(fragment, matchActivity);
        mDevice.pressBack();
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

    private void testMatchScoreFragment(final MatchScoreFragment fragment, final MatchActivity activity) {
        final int scoreRight = random.nextInt(10);
        final int winsLeft = random.nextInt(10);
        final int winsRight = random.nextInt(10);
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                fragment.onScore(Side.RIGHT, scoreRight);
                fragment.onWin(Side.LEFT, winsLeft);
                fragment.onWin(Side.RIGHT, winsRight);
            }
        });
        TextView textView = activity.findViewById(R.id.right_score);
        assertEquals(String.valueOf(scoreRight), textView.getText());
        textView = activity.findViewById(R.id.left_games);
        assertEquals(String.valueOf(winsLeft), textView.getText());
        textView = activity.findViewById(R.id.right_games);
        assertEquals(String.valueOf(winsRight), textView.getText());
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                fragment.onMatchEnded(Side.LEFT.toString());
            }
        });
        try{
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // Unfortunately need to wait for a moment otherwise it won't find the textView
        }
        textView = activity.findViewById(R.id.winner_name);
        assertEquals(Side.LEFT.toString(), textView.getText());
    }

    private Activity getActivityInstance(){
        final Activity[] currentActivity = {null};

        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                Collection<Activity> resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                Iterator<Activity> it = resumedActivity.iterator();
                currentActivity[0] = it.next();
            }
        });

        return currentActivity[0];
    }
}