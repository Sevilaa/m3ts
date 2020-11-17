package com.android.grafika;

import android.Manifest;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.junit.After;
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
import cz.fmo.R;
import helper.GrantPermission;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PlayMovieSurfaceActivityTest {
    private PlayMovieSurfaceActivity playMovieSurfaceActivity;
    private SurfaceView surfaceViewMovie;
    private SurfaceView surfaceViewTracks;
    private SurfaceView surfaceViewTable;
    private Spinner movieSelectSpinner;
    private Button playStopButton;
    private TextView txtSide;
    private TextView txtBounce;
    private TextView txtScoreLeft;
    private TextView txtScoreRight;
    private TextView txtGameLeft;
    private TextView txtGameRight;

    @Rule
    public GrantPermissionRule grantPermissionRuleCamera = GrantPermissionRule.grant(android.Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);

    @Rule
    public ActivityTestRule<PlayMovieSurfaceActivity> pmsActivityRule = new ActivityTestRule<PlayMovieSurfaceActivity>(PlayMovieSurfaceActivity.class);

    @Before
    public void grantAllPermissions() {
        GrantPermission.grantAllPermissions();
    }

    @Before
    public void setUp() throws Exception {
        playMovieSurfaceActivity = pmsActivityRule.getActivity();
        surfaceViewMovie = playMovieSurfaceActivity.findViewById(R.id.playMovie_surface);
        surfaceViewTracks = playMovieSurfaceActivity.findViewById(R.id.playMovie_surfaceTracks);
        surfaceViewTable = playMovieSurfaceActivity.findViewById(R.id.playMovie_surfaceTable);
        movieSelectSpinner = playMovieSurfaceActivity.findViewById(R.id.playMovieFile_spinner);
        playStopButton = playMovieSurfaceActivity.findViewById(R.id.play_stop_button);
        txtSide = playMovieSurfaceActivity.findViewById(R.id.txtSide);
        txtBounce = playMovieSurfaceActivity.findViewById(R.id.txtBounce);
        txtScoreLeft = playMovieSurfaceActivity.findViewById(R.id.txtPlayMovieScoreLeft);
        txtScoreRight = playMovieSurfaceActivity.findViewById(R.id.txtPlayMovieScoreRight);
        txtGameLeft = playMovieSurfaceActivity.findViewById(R.id.txtPlayMovieGameLeft);
        txtGameRight = playMovieSurfaceActivity.findViewById(R.id.txtPlayMovieGameRight);
    }

    @After
    public void tearDown() throws Exception {
        playMovieSurfaceActivity.finish();
        playMovieSurfaceActivity = null;
    }

    @Test
    // plays a video for a couple of seconds (with bounces in it), and then checks if there was a bounce
    public void testPlayMovieAndFindBounces() {
        findAllViewsInActivity();
        movieSelectSpinner.setSelection(0, true);
        playMovieSurfaceActivity.onItemSelected(movieSelectSpinner, null, 0, R.id.playMovieFile_spinner);
        onView(withId(R.id.play_stop_button))
                .perform(click());
        onView(isRoot()).perform(waitFor(10000));
        assertNotEquals("0", txtBounce.getText());
        assertNotEquals("None", txtSide.getText());
        assertEquals(playMovieSurfaceActivity.getResources().getString(R.string.stop_button_text),playStopButton.getText());
        onView(withId(R.id.play_stop_button))
                .perform(click());
    }

    private void findAllViewsInActivity() {
        assertNotNull(surfaceViewMovie);
        assertNotNull(surfaceViewTracks);
        assertNotNull(surfaceViewTable);
        assertNotNull(movieSelectSpinner);
        assertNotNull(playStopButton);
        assertNotNull(txtSide);
        assertNotNull(txtBounce);
        assertNotNull(txtGameLeft);
        assertNotNull(txtGameRight);
        assertNotNull(txtScoreLeft);
        assertNotNull(txtScoreRight);
        assertEquals(playMovieSurfaceActivity.getResources().getString(R.string.play_button_text), playStopButton.getText());
        assertEquals("0", txtBounce.getText());
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