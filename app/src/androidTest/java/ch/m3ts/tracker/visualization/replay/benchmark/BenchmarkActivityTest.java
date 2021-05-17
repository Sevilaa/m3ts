package ch.m3ts.tracker.visualization.replay.benchmark;

import android.Manifest;
import android.app.Activity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import ch.m3ts.MainActivity;
import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.Subscribable;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.data.eventdetector.EventDetectorEventData;
import ch.m3ts.eventbus.data.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.events.EventDetectionListener;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import cz.fmo.Lib;
import cz.fmo.R;
import cz.fmo.data.Track;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.m3ts.tracker.visualization.replay.ReplayActivityTest.waitFor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BenchmarkActivityTest {
    static class StubListener implements Subscribable {
        private final EventDetectionListener eventDetectionListener;
        private final DisplayUpdateListener displayUpdateListener;

        StubListener(EventDetectionListener eventDetectionListener, DisplayUpdateListener displayUpdateListener) {
            this.eventDetectionListener = eventDetectionListener;
            this.displayUpdateListener = displayUpdateListener;
        }

        @Override
        public void handle(Event<?> event) {
            Object data = event.getData();
            if (data instanceof EventDetectorEventData) {
                EventDetectorEventData eventDetectorData = (EventDetectorEventData) data;
                eventDetectorData.call(eventDetectionListener);
            } else if (data instanceof ToDisplayData) {
                ToDisplayData displayData = (ToDisplayData) data;
                displayData.call(displayUpdateListener);
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
    private DisplayUpdateListener displayUpdateListener;

    @Before
    public void init() {
        eventDetectionListener = mock(EventDetectionListener.class);
        displayUpdateListener = mock(DisplayUpdateListener.class);

        StubListener stubListener = new StubListener(eventDetectionListener, displayUpdateListener);
        TTEventBus.getInstance().register(stubListener);
    }

    @Test
    public void testBenchmark() {
        Activity activity = pmsMainActivityRule.getActivity();
        onView(withId(R.id.live_settings_button)).perform(click());
        onView(withText(R.string.prefHeaderDebug)).perform(click());
        onView(withText(R.string.prefBenchmark)).perform(click());
        onView(withText(R.string.benchmark_play_button_text)).perform(click());
        onView(isRoot()).perform(waitFor(20000));
        verify(eventDetectionListener, atLeastOnce()).onBounce((Lib.Detection) any(), (Side) any());
        verify(eventDetectionListener, atLeastOnce()).onSideChange((Side) any());
        verify(eventDetectionListener, atLeastOnce()).onStrikeFound((Track) any());
        verify(displayUpdateListener, atLeastOnce()).onScore((Side) any(), anyInt(), (Side) any(), (Side) any());
    }
}
