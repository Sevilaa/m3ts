package ch.m3ts.display;

import android.Manifest;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.view.View;
import android.widget.ProgressBar;
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
import ch.m3ts.eventbus.TTEvent;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.data.StatusUpdateData;
import ch.m3ts.tabletennis.helper.Side;
import cz.fmo.R;
import cz.fmo.util.Config;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasTextColor;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotEquals;

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
        final int[] xLocations = new int[2];
        final int[] yLocations = new int[2];
        Point[] corners = matchSelectCornerFragment.getTableCorners();
        assertEquals(2, corners.length);
        for (Point corner : corners) {
            assertNull(corner);
        }

        // long click 2 times on random locations on the screen and check if table corners have been selected
        for (int i = 0; i<2; i++) {
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

        // now de-select all corners by hitting the revert button
        onView(withId(R.id.init_revertButton))
                .perform(click());
        onView(withId(R.id.init_revertButton))
                .perform(click());
        assertNull(corners[0]);
        assertNull(corners[1]);
        onView(withId(R.id.init_startMatch)).check(matches(not(isDisplayed())));

        // hit revert button some more
        testClickingRevertTooManyTimes();
    }

    @Test
    public void testStartingGame() throws Exception {
        switchToCornerSelection();
        for (int i = 0; i<2; i++) {
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
        final int scoreLeft = random.nextInt(10);
        final int winsLeft = random.nextInt(10);
        final int winsRight = random.nextInt(10);
        final String playerLeft = "left";
        final String playerRight = "right";

        // invoke onReadyToServe and check if ui updated
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchScoreFragment.onReadyToServe(Side.RIGHT);
            }
        });
        onView(withId(R.id.right_score)).check(matches(hasTextColor(R.color.display_serving)));
        onView(withId(R.id.left_score)).check(matches(hasTextColor(R.color.primary_light)));
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchScoreFragment.onReadyToServe(Side.LEFT);
            }
        });
        onView(withId(R.id.left_score)).check(matches(hasTextColor(R.color.display_serving)));
        onView(withId(R.id.right_score)).check(matches(hasTextColor(R.color.primary_light)));

        // invoke onStatusUpdate and check if value matches
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchScoreFragment.onStatusUpdate(playerLeft, playerRight, scoreLeft, scoreRight, winsLeft, winsRight, Side.LEFT, 2);
            }
        });
        onView(withId(R.id.left_games)).check(matches(withText(containsString("0"+winsLeft))));
        onView(withId(R.id.right_games)).check(matches(withText(containsString("0"+winsRight))));
        onView(withId(R.id.right_score)).check(matches(withText(containsString("0"+scoreRight))));
        onView(withId(R.id.left_score)).check(matches(withText(containsString("0"+scoreLeft))));
        assertEquals(playerLeft, ((TextView)matchActivity.findViewById(R.id.left_name)).getText().toString());
        assertEquals(playerRight, ((TextView)matchActivity.findViewById(R.id.right_name)).getText().toString());

        // invoke onScore and check if value matches
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchScoreFragment.onScore(Side.RIGHT, scoreRight, Side.LEFT, Side.LEFT);
            }
        });
        TextView textView = matchActivity.findViewById(R.id.right_score);
        assertEquals("0"+scoreRight, textView.getText());

        // invoke onWin and check if values match (need to separate onScore and onWin as onWin clears the score points)
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                matchScoreFragment.onWin(Side.LEFT, winsLeft);
                matchScoreFragment.onWin(Side.RIGHT, winsRight);
            }
        });
        textView = matchActivity.findViewById(R.id.left_games);
        assertEquals("0" + winsLeft, textView.getText());
        textView = matchActivity.findViewById(R.id.right_games);
        assertEquals("0" + winsRight, textView.getText());

        // click on mirror-button
        onView(withId(R.id.btnMirrorLayout)).perform(click());
        TTEventBus.getInstance().dispatch(new TTEvent<>(new StatusUpdateData("hans", "peter", 3, 5, 1, 0, Side.RIGHT, 3)));
        onView(withId(R.id.right_name)).check(matches(withText("peter")));
        onView(withId(R.id.left_name)).check(matches(withText("hans")));
        onView(withId(R.id.right_score)).check(matches(withText("05")));

        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                matchScoreFragment.onMatchEnded(Side.LEFT.toString());
            }
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // Unfortunately need to wait for a moment otherwise it won't find the textView
        }
        String title = matchActivity.getString(R.string.msDialogTitle);
        title = String.format(title, Side.LEFT.toString());
        onView(withText(title))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testMatchScoreFragmentPause() throws Exception {
        // switch to matchScoreFragment
        final MatchScoreFragment matchScoreFragment = new MatchScoreFragment();
        matchActivity.replaceFragment(matchScoreFragment, "MATCH_SCORE");
        Thread.sleep(1000);
        assertEquals(matchActivity.findViewById(R.id.btnPauseResumeReferee).getTag(), "Play");
        onView(withId(R.id.btnPauseResumeReferee)).perform(click());
        assertEquals(matchActivity.findViewById(R.id.btnPauseResumeReferee).getTag(), "Pause");
        assertNotEquals("Play", matchActivity.findViewById(R.id.btnPauseResumeReferee).getTag());
    }

    @Test
    public void testMatchInitFragment() throws Exception {
        // switch to matchInitFragment
        final MatchInitFragment matchInitFragment = new MatchInitFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", Integer.toString(0));
        bundle.putString("server", Integer.toString(0));
        matchInitFragment.setArguments(bundle);
        matchActivity.replaceFragment(matchInitFragment, "MATCH_INIT");
        Thread.sleep(1000);
        Config config = new Config(matchActivity);
        if (config.isUsingPubnub()) {
            onView(withId(R.id.qr_code)).check(matches(isDisplayed()));
        } else {
            onView(withText(R.string.connectDisplaySearching)).check(matches(isDisplayed()));
        }

        // invoke onImageTransmissionStarted and check if LoadingBar is initialized
        ProgressBar bar = matchActivity.findViewById(R.id.loading_bar);
        final int parts = 50;
        assertEquals(100, bar.getMax());
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                matchInitFragment.onImageTransmissionStarted(parts);
            }
        });
        assertEquals(parts, bar.getMax());

        // invoke onImagePartReceived and check if LoadingBar is updated
        final int partNumber = 10;
        assertEquals(0, bar.getProgress());
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchInitFragment.onImagePartReceived(partNumber);
            }
        });
        assertEquals(partNumber, bar.getProgress());


        // invoke onConnected and check if ui is updated
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchInitFragment.onConnected();
            }
        });
        onView(withText(R.string.miConnectedTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.miConnectedSubTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.miPictureBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.qr_code)).check(matches(not(isDisplayed())));

        // invoke onClick and check if ui is updated
        final View onClickView = new View(matchActivity);
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){

                onClickView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Empty Listener just to check if onclick is deactivated
                    }
                });
                matchInitFragment.onClick(onClickView);
            }

        });
        onView(withText(R.string.miPictureLoadingSubTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.display_loading)).check(matches(isDisplayed()));
        onView(withId(R.id.miPictureBtn)).check(matches(not(isDisplayed())));
        assertFalse(onClickView.hasOnClickListeners());

        // invoke onImageReceived and check if fragment changes
        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                matchInitFragment.onImageReceived(new byte[]{}, 100, 100);
            }
        });
        onView(withId(R.id.miTitle)).check(doesNotExist());
        onView(withId(R.id.init_description)).check(matches(isDisplayed()));
    }

    private void testClickingRevertTooManyTimes() {
        final int TOO_MANY_TIMES = 5;
        for (int i = 0; i<TOO_MANY_TIMES; i++) {
            onView(withId(R.id.init_revertButton))
                    .perform(click());

            Point[] corners = matchSelectCornerFragment.getTableCorners();
            assertEquals(2, corners.length);
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
        assertEquals(2, corners.length);
        for (int i=0; i<2; i++) {
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
        onView(withId(R.id.init_startMatch))
                .check(matches(not(isDisplayed())));
    }
}