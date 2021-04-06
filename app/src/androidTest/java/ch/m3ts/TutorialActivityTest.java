package ch.m3ts;

import android.Manifest;

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
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TutorialActivityTest {
    private static final int STEPS_TRACKER_TUTORIAL = 3;
    private static final int STEPS_DISPLAY_TUTORIAL = 5;
    private static final int STEPS_SERVE_TUTORIAL = 1;

    @Rule
    public GrantPermissionRule grantPermissionRuleCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);

    @Rule
    public ActivityTestRule<MainActivity> pmsMainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void grantAllPermissions() {
        GrantPermission.grantAllPermissions();
    }

    @Test
    public void checkTutorialActivity() {
        int[] tutorials = {
                R.string.tutorialTrackerBtnLabel,
                R.string.tutorialDisplayBtnLabel,
                R.string.tutorialPlayBtnLabel
        };
        onView(withId(R.id.mainHowToPlay)).perform(click());
        for (int tutorialButtonLabel : tutorials) {
            onView(withText(tutorialButtonLabel)).check(matches(isDisplayed()));
        }
        onView(withText(tutorials[0])).perform(click());

        // should be in fragment now, confirm with display matches
        onView(withId(R.id.tutorialImg)).check(matches(isDisplayed()));
        onView(withId(R.id.tutorialDescriptionTxt)).check(matches(isDisplayed()));
        onView(withId(R.id.tutorialTitleTxt)).check(matches(isDisplayed()));

        // click revert Button a couple of times, nothing should happen
        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.tutorialGoBackBtn)).perform(click());
        }

        onView(withId(R.id.tutorialContinueBtn)).perform(click());
        onView(withId(R.id.tutorialGoBackBtn)).perform(click());

        // do all tutorials...
        testTrackerTutorial();
        onView(withText(tutorials[1])).perform(click());
        testDisplayTutorial();
        onView(withText(tutorials[2])).perform(click());
        testServeTutorial();

        // now we should be in mainActivity again
        pressBack();
        onView(withId(R.id.mainHowToPlay)).check(matches(isDisplayed()));
    }

    private void testTrackerTutorial() {
        // press continue until we finished tutorial
        for (int i = 0; i < STEPS_TRACKER_TUTORIAL; i++) {
            onView(withId(R.id.tutorialContinueBtn)).perform(click());
        }
        onView(withId(R.id.mainHowToPlay)).perform(click());
    }

    private void testDisplayTutorial() {
        // press continue until we finished tutorial
        for (int i = 0; i < STEPS_DISPLAY_TUTORIAL; i++) {
            onView(withId(R.id.tutorialContinueBtn)).perform(click());
        }
        onView(withId(R.id.mainHowToPlay)).perform(click());
    }

    private void testServeTutorial() {
        // press continue until we finished tutorial
        for (int i = 0; i < STEPS_SERVE_TUTORIAL; i++) {
            onView(withId(R.id.tutorialContinueBtn)).perform(click());
        }
        onView(withId(R.id.mainHowToPlay)).perform(click());
    }
}