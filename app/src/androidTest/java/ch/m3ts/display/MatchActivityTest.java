package ch.m3ts.display;

import android.Manifest;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.view.View;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import ch.m3ts.tabletennis.helper.Side;
import cz.fmo.R;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MatchActivityTest extends InstrumentationTestCase {
    private TextView txtSelectedCorners;
    private TextView txtMaxCorners;
    private MatchActivity matchActivity;
    private MatchSelectCornerFragment matchSelectCornerFragment;
    private Random random = new Random();

    @Rule
    public ActivityTestRule<MatchActivity> initActivityRule = new ActivityTestRule<MatchActivity>(MatchActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            // add data to intend as this activity is only called with a int array
            // (representing corner points of the table)
            Intent intent = new Intent(Intent.ACTION_MAIN);
            Bundle bundle = new Bundle();
            bundle.putBoolean("isRestartedMatch", false);
            intent.putExtras(bundle);
            return intent;
        }
    };

    @Rule
    public GrantPermissionRule grantPermissionRuleCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);

    @Before
    public void grantAllPermissions() {
        GrantPermission.grantAllPermissions();
    }

    @Before
    public void setUp() {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        matchActivity = initActivityRule.getActivity();
    }

    private void switchToCornerSelection() throws Exception {
        byte[] randomBytes = new byte[200];
        new Random().nextBytes(randomBytes);
        matchSelectCornerFragment = new MatchSelectCornerFragment();
        Bundle bundle = new Bundle();
        bundle.putByteArray("tableFrame", randomBytes);
        bundle.putInt("width", 20);
        bundle.putInt("height", 10);
        matchSelectCornerFragment.setArguments(bundle);
        matchActivity.replaceFragment(matchSelectCornerFragment, "MATCH_SELECT_CORNERS");
        Thread.sleep(1000);
        txtSelectedCorners = matchActivity.findViewById(R.id.init_cornersSelectedTxt);
        txtMaxCorners = matchActivity.findViewById(R.id.init_cornersSelectedMaxTxt);
    }

    @After
    public void tearDown() {
        matchActivity.finish();
        matchSelectCornerFragment = null;
        matchActivity = null;
    }

    @Test
    public void testSelectFourCorners() throws Exception {
        switchToCornerSelection();
        findAllViewsOfCornerSelection();
        final int[] xLocations = new int[4];
        final int[] yLocations = new int[4];
        Point[] corners = matchSelectCornerFragment.getTableCorners();
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

        // now de-select all corners by hitting the revert button 4 times
        for (int i=3; i>=0; i--) {
            onView(withId(R.id.init_revertButton))
                    .perform(click());
            assertNull(corners[i]);
            assertNull(matchActivity.findViewById(R.id.init_startGameBtn));
        }

        // hit revert button some more
        testClickingRevertTooManyTimes();
    }

    @Test
    public void testStartingGame() throws Exception {
        switchToCornerSelection();
        for (int i = 0; i<4; i++) {
            int x = Math.round(100 * (float) Math.random());
            int y = Math.round(100 * (float) Math.random());
            onView(withId(R.id.init_zoomLayout))
                    .perform(longClickXY(x,y));
        }

        // click on "Start Game"
        onView(withId(R.id.init_startMatch))
                .perform(click());

        // check if we switched activity by checking if some views are displayed
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
    public void testMatchScoreFragment() throws Exception {
        // switch to matchScoreFragment
        final MatchScoreFragment matchScoreFragment = new MatchScoreFragment();
        matchActivity.replaceFragment(matchScoreFragment, "MATCH_SCORE");
        Thread.sleep(1000);

        final int scoreRight = random.nextInt(10);
        final int winsLeft = random.nextInt(10);
        final int winsRight = random.nextInt(10);

        // invoke onScore and check if value matches
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchScoreFragment.onScore(Side.RIGHT, scoreRight, Side.LEFT);
            }
        });
        TextView textView = matchActivity.findViewById(R.id.right_score);
        assertEquals("0"+scoreRight, textView.getText());

        // invoke onWin and check if values match (need to separate onScore and onWin as onWin clears the score points)
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchScoreFragment.onWin(Side.LEFT, winsLeft);
                matchScoreFragment.onWin(Side.RIGHT, winsRight);
            }
        });
        textView = matchActivity.findViewById(R.id.left_games);
        assertEquals("0"+winsLeft, textView.getText());
        textView = matchActivity.findViewById(R.id.right_games);
        assertEquals("0"+winsRight, textView.getText());
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchScoreFragment.onMatchEnded(Side.LEFT.toString());
            }
        });
        try{
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // Unfortunately need to wait for a moment otherwise it won't find the textView
        }
        onView(withText(Side.LEFT.toString()))
                .check(matches(isDisplayed()));
        onView(withText(R.string.mwWon))
                .check(matches(isDisplayed()));
    }

    private void testClickingRevertTooManyTimes() {
        final int TOO_MANY_TIMES = 5;
        for (int i = 0; i<TOO_MANY_TIMES; i++) {
            onView(withId(R.id.init_revertButton))
                    .perform(click());

            Point[] corners = matchSelectCornerFragment.getTableCorners();
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

        Point[] corners = matchSelectCornerFragment.getTableCorners();
        assertEquals(4, corners.length);
        for (int i=0; i<4; i++) {
            assertEquals(xLocations[i], corners[i].x);
            assertEquals(yLocations[i], corners[i].y);
        }
    }

    private static ViewAction longClickXY(final int x, final int y){
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

    private void findAllViewsOfCornerSelection() {
        onView(withId(R.id.init_zoomLayout))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_revertButton))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_cornersSelectedTxt))
                .check(matches(isDisplayed()));
        onView(withId(R.id.init_cornersSelectedMaxTxt))
                .check(matches(isDisplayed()));
        assertEquals(String.valueOf(matchSelectCornerFragment.getTableCorners().length), txtMaxCorners.getText());
        // we shouldn't see the "Start Game" button - as no corners have been selected
        onView(withId(R.id.init_startGameBtn))
                .check(doesNotExist());
    }
}