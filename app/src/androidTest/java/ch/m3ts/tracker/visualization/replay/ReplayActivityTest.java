package ch.m3ts.tracker.visualization.replay;

import android.Manifest;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import ch.m3ts.MainActivity;
import ch.m3ts.detection.EventDetectionListener;
import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.Subscribable;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.event.ball.EventDetectorEventData;
import ch.m3ts.util.Side;
import cz.fmo.Lib;
import cz.fmo.R;
import cz.fmo.data.Track;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ReplayActivityTest {

    static class StubListener implements Subscribable {
        private final EventDetectionListener eventDetectionListener;

        StubListener(EventDetectionListener eventDetectionListener) {
            this.eventDetectionListener = eventDetectionListener;
        }

        @Override
        public void handle(Event<?> event) {
            Object data = event.getData();
            if (data instanceof EventDetectorEventData) {
                EventDetectorEventData eventDetectorData = (EventDetectorEventData) data;
                eventDetectorData.call(eventDetectionListener);
            }
        }
    }

    @Rule
    public GrantPermissionRule grantPermissionRuleCamera = GrantPermissionRule.grant(android.Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);

    @Rule
    public ActivityTestRule<MainActivity> pmsMainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void grantAllPermissions() {
        GrantPermission.grantAllPermissions();
    }

    private EventDetectionListener eventDetectionListener;

    @Before
    public void init() {
        eventDetectionListener = mock(EventDetectionListener.class);
        StubListener stubListener = new StubListener(eventDetectionListener);
        TTEventBus.getInstance().register(stubListener);
    }

    @Test
    // plays a video for a couple of seconds (with bounces in it), and then checks if there was a bounce
    public void testPlayMovieAndFindBounces() {
        MainActivity activity = pmsMainActivityRule.getActivity();
        onView(withId(R.id.live_settings_button))
                .perform(click());
        onView(withText(R.string.prefHeaderDebug))
                .perform(click());
        onView(withText(R.string.runVideoPlayer))
                .perform(click());
        findAllViewsInActivity();
        onView(withId(R.id.playMovieFile_spinner))
                .perform(click());
        onData(anything()).atPosition(0).perform(click());
        onView(withId(R.id.play_stop_button))
                .perform(click());
        verifyIfSwipeScoreManipulationWorks();
        onView(isRoot()).perform(waitFor(15000));
        onView(allOf(withId(R.id.txtBounce), not(withText("0"))));
        onView(allOf(withId(R.id.txtSide), not(withText("None"))));
        onView(withId(R.id.play_stop_button))
                .check(matches(withText(R.string.stop_button_text)));
        onView(withId(R.id.play_stop_button))
                .perform(click());
        verify(eventDetectionListener, atLeastOnce()).onStrikeFound((Track) any());
        verify(eventDetectionListener, atLeastOnce()).onTableSideChange(Side.LEFT);
        verify(eventDetectionListener, atLeastOnce()).onTableSideChange(Side.RIGHT);
        verify(eventDetectionListener, atLeastOnce()).onSideChange(Side.LEFT);
        verify(eventDetectionListener, atLeastOnce()).onSideChange(Side.RIGHT);
        verify(eventDetectionListener, atLeastOnce()).onBounce((Lib.Detection) any(), (Side) any());
    }

    private void verifyIfSwipeScoreManipulationWorks() {
        // swipe 3 times to see if score changes
        onView(withId(R.id.playMovie_surface))
                .perform(swipeUp());
        onView(withId(R.id.playMovie_surface))
                .perform(swipeUp());
        onView(withId(R.id.playMovie_surface))
                .perform(swipeUp());
        onView(withText("3"))
                .check(matches(isDisplayed()));
        onView(withId(R.id.playMovie_surface))
                .perform(swipeDown());
        onView(withId(R.id.playMovie_surface))
                .perform(swipeDown());
        onView(allOf(withId(R.id.txtPlayMovieScoreRight), withText("1")));
        // no score changes on left or right swipe
        onView(withId(R.id.playMovie_surface))
                .perform(swipeLeft());
        onView(withId(R.id.playMovie_surface))
                .perform(swipeRight());
        onView(allOf(withId(R.id.txtPlayMovieScoreRight), withText("1")));
    }

    private void findAllViewsInActivity() {
        onView(withId(R.id.playMovie_surface))
                .check(matches(isDisplayed()));
        onView(withId(R.id.playMovie_surfaceTracks))
                .check(matches(isDisplayed()));
        onView(withId(R.id.playMovie_surfaceTable))
                .check(matches(isDisplayed()));
        onView(withId(R.id.playMovieFile_spinner))
                .check(matches(isDisplayed()));
        onView(withId(R.id.play_stop_button))
                .check(matches(isDisplayed()));
        onView(withId(R.id.txtSide))
                .check(matches(isDisplayed()));
        onView(withId(R.id.txtBounce))
                .check(matches(isDisplayed()));
        onView(withId(R.id.txtPlayMovieScoreLeft))
                .check(matches(isDisplayed()));
        onView(withId(R.id.txtPlayMovieScoreRight))
                .check(matches(isDisplayed()));
        onView(withId(R.id.txtPlayMovieGameLeft))
                .check(matches(isDisplayed()));
        onView(withId(R.id.txtPlayMovieGameRight))
                .check(matches(isDisplayed()));
        onView(withId(R.id.play_stop_button))
                .check(matches(withText(R.string.play_button_text)));
        onView(withId(R.id.txtBounce))
                .check(matches(withText("0")));
    }

    /**
     * Perform action of waiting for a specific time.
     */
    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}