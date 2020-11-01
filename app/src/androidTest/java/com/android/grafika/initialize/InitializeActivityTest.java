package com.android.grafika.initialize;

import android.Manifest;
import android.graphics.Point;
import android.test.InstrumentationTestCase;
import android.view.View;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import cz.fmo.R;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class InitializeActivityTest extends InstrumentationTestCase {
    private TextView txtSelectedCorners;
    private TextView txtMaxCorners;
    private InitializeActivity initializeActivity;

    @Rule
    public ActivityTestRule<InitializeActivity> initActivityRule = new ActivityTestRule<>(InitializeActivity.class);

    @Rule
    public GrantPermissionRule grantPermissionRuleCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);

    @Before
    public void grantAllPermissions() {
        GrantPermission.grantAllPermissions();
    }

    @Before
    public void setUp() throws Exception {
        initializeActivity = initActivityRule.getActivity();
        txtSelectedCorners = initializeActivity.findViewById(R.id.init_cornersSelectedTxt);
        txtMaxCorners = initializeActivity.findViewById(R.id.init_cornersSelectedMaxTxt);
    }

    @After
    public void tearDown() throws Exception {
        initializeActivity.finish();
        initializeActivity = null;
    }

    @Test
    public void testSelectFourCorners() {
        findAllViewsOnStartup();
        final int[] xLocations = new int[4];
        final int[] yLocations = new int[4];
        Point[] corners = initializeActivity.getTableCorners();
        assertEquals(4, corners.length);
        for (Point corner : corners) {
            assertNull(corner);
        }

        // long click 4 times on random locations on the screen
        for (int i = 0; i<4; i++) {
            int x = Math.round(100 * (float) Math.random());
            int y = Math.round(100 * (float) Math.random());
            xLocations[i] = x;
            yLocations[i] = y;
            onView(withId(R.id.init_zoomLayout))
                    .perform(longClickXY(x,y));
            assertNotNull(corners[i]);
            assertEquals(x, corners[i].x);
            assertEquals(y, corners[i].y);
            assertEquals(String.valueOf(i+1), txtSelectedCorners.getText());
        }

        // long click some more
        testLongClickingScreenTooManyTimes(xLocations, yLocations);

        // fragment should have changed, startGame button should be there
        assertNotNull(initializeActivity.findViewById(R.id.init_startGameBtn));

        // now de-select all corners by hitting the revert button 4 times
        for (int i=3; i>=0; i--) {
            onView(withId(R.id.init_revertButton))
                    .perform(click());
            assertNull(corners[i]);
            assertNull(initializeActivity.findViewById(R.id.init_startGameBtn));
        }

        // hit revert button some more
        testClickingRevertTooManyTimes();
    }

    @Test
    public void testStartingGame() {
        for (int i = 0; i<4; i++) {
            int x = Math.round(100 * (float) Math.random());
            int y = Math.round(100 * (float) Math.random());
            onView(withId(R.id.init_zoomLayout))
                    .perform(longClickXY(x,y));
        }
        onView(withId(R.id.init_startGameBtn))
                .perform(click());

        // check if we switched activity by checking if some views are displayed
        onView(withId(R.id.txtPlayMovieScoreLeft))
                .check(matches(isDisplayed()));
        onView(withId(R.id.playMovie_debugGrid))
                .check(matches(isDisplayed()));
    }

    private void testClickingRevertTooManyTimes() {
        final int TOO_MANY_TIMES = 5;
        for (int i = 0; i<TOO_MANY_TIMES; i++) {
            onView(withId(R.id.init_revertButton))
                    .perform(click());

            Point[] corners = initializeActivity.getTableCorners();
            assertEquals(4, corners.length);
            for (Point corner : corners) {
                assertNull(corner);
            }
        }
    }

    private void testLongClickingScreenTooManyTimes(int[] xLocations, int[] yLocations) {
        final int TOO_MANY_TIMES = 5;
        // then click too many times on the screen
        for (int i = 0; i<TOO_MANY_TIMES; i++) {
            int x = Math.round(100 * (float) Math.random());
            int y = Math.round(100 * (float) Math.random());
            onView(withId(R.id.init_zoomLayout))
                    .perform(longClickXY(x,y));
        }

        Point[] corners = initializeActivity.getTableCorners();
        assertEquals(4, corners.length);
        for (int i=0; i<4; i++) {
            assertEquals(xLocations[i], corners[i].x);
            assertEquals(yLocations[i], corners[i].y);
        }
    }

    public static ViewAction longClickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.LONG,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        return new float[] {screenX, screenY};
                    }
                },
                Press.FINGER);
    }

    private void findAllViewsOnStartup() {
        onView(withId(R.id.init_zoomLayout))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_revertButton))
                .check(matches(isDisplayed()));
        onView(withId(R.id.playMovie_surface))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_cornersSelectedTxt))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_cornersSelectedMaxTxt))
                .check(matches(isDisplayed()));
        assertEquals(String.valueOf(initializeActivity.getTableCorners().length), txtMaxCorners.getText());
        // we shouldn't see the "Start Game" button - as no corners have been selected
        onView(withId(R.id.init_startGameBtn))
                .check(doesNotExist());
    }
}
