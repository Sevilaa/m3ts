package ch.m3ts.display;

import android.content.Intent;
import android.os.Bundle;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import cz.fmo.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.m3ts.tracker.visualization.replay.ReplayActivityTest.waitFor;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MatchWonActivityTest extends InstrumentationTestCase {
    private MatchWonActivity activity;
    private String TEST_NAME = "testname";
    private String TEST_ROOM = "testroom";

    @Rule
    public ActivityTestRule<MatchWonActivity> initActivityRule = new ActivityTestRule<MatchWonActivity>(MatchWonActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            Bundle bundle = new Bundle();
            bundle.putString("winner", TEST_NAME);
            bundle.putString("room", TEST_ROOM);
            intent.putExtras(bundle);
            return intent;
        }
    };

    @Before
    public void setUp() {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        activity = initActivityRule.getActivity();
    }

    @After
    public void tearDown() {
        activity.finish();
        activity = null;
    }

    @Test
    public void winnerNameDisplayed() {
        onView(withId(R.id.winner_name))
                .check(matches(withText(TEST_NAME)));
    }

    @Test
    public void onClickPlayAgain() {
        onView(withId(R.id.play_again))
                .perform(click());
        onView(isRoot()).perform(waitFor(4000));
        onView(withId(R.id.left_score))
                .check(matches(isDisplayed()));
        onView(withId(R.id.right_score))
                .check(matches(isDisplayed()));
        onView(withId(R.id.right_score))
                .check(matches(withText("00")));
        onView(withId(R.id.btnPauseResumeReferee))
                .check(matches(isDisplayed()));
    }

    @Test
    public void onClickBackToMenu() {
        onView(withId(R.id.back_to_menu))
                .perform(click());
        onView(withId(R.id.mainUseAsTrackerBtn))
                .check(matches(isDisplayed()));
        onView(withId(R.id.mainUseAsDisplayBtn))
                .check(matches(isDisplayed()));
        onView(withId(R.id.live_settings_button))
                .check(matches(isDisplayed()));
    }
}