package ch.m3ts.display;

import android.content.Intent;
import android.os.Bundle;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.test.espresso.action.ViewActions;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import ch.m3ts.display.statistic.GameStats;
import ch.m3ts.display.statistic.MatchStats;
import ch.m3ts.display.statistic.PointData;
import ch.m3ts.display.statistic.TrackData;
import ch.m3ts.eventbus.TTEvent;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.event.StatsData;
import ch.m3ts.util.Side;
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
        onView(isRoot()).perform(waitFor(1000));
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
    public void onClickMatchStats() {
        String playerLeftName = "hannes";
        String playerRightName = "kannes";
        String gameStatsButtonLabel = "Stats";
        onView(withId(R.id.show_stats)).perform(click());
        onView(withText(R.string.mstLoadingMsg)).check(matches(isDisplayed()));
        List<PointData> points = new ArrayList<>();
        points.add(new PointData("msg1", new ArrayList<TrackData>(), Side.LEFT, 9, 2, Side.RIGHT, Side.RIGHT, Side.LEFT, 12));
        points.add(new PointData("msg2", new ArrayList<TrackData>(), Side.LEFT, 10, 2, Side.RIGHT, Side.RIGHT, Side.LEFT, 10));
        points.add(new PointData("msg3", new ArrayList<TrackData>(), Side.LEFT, 11, 2, Side.RIGHT, Side.RIGHT, Side.LEFT, 3));
        List<GameStats> games = new ArrayList<>();
        GameStats gameStats = new GameStats(points);
        games.add(gameStats);
        Map<Side, Integer> tableCorners = new HashMap<>();
        tableCorners.put(Side.LEFT, 60);
        tableCorners.put(Side.RIGHT, 1130);
        MatchStats stats = new MatchStats(games, playerLeftName, playerRightName, "12.12.2021", tableCorners);
        TTEventBus.getInstance().dispatch(new TTEvent<>(new StatsData(stats)));
        onView(isRoot()).perform(waitFor(1000));
        onView(withId(R.id.player_left)).check(matches(withText(playerLeftName)));
        onView(withId(R.id.player_right)).check(matches(withText(playerRightName)));
        onView(withId(R.id.score)).check(matches(withText("1:0")));
        onView(withText(gameStatsButtonLabel))
                .perform(ViewActions.scrollTo())
                .check(matches(isDisplayed()));
        onView(withText(gameStatsButtonLabel)).perform(click());
        onView(withId(R.id.score)).check(matches(withText("11:2")));
        onView(withId(R.id.game_history))
                .perform(ViewActions.scrollTo())
                .check(matches(isDisplayed()));
        onView(withText("9:2")).perform(click());
        onView(withText("msg1")).check(matches(isDisplayed()));
        onView(withText("12s")).check(matches(isDisplayed()));
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