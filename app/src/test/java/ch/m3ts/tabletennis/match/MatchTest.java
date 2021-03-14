package ch.m3ts.tabletennis.match;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.m3ts.tabletennis.events.GestureCallback;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.game.GameType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MatchTest {
    private Match match;
    private GestureCallback gestureCallback;
    private UICallback uiCallback;
    private final String PLAYER_1_NAME = "Test_Spieler";
    private final String PLAYER_2_NAME = "Test_Spieler_Der_Zweite";
    private final Side startingServerSide = Side.LEFT;

    @Before
    public void setUp() {
        uiCallback = mock(UICallback.class);
        gestureCallback = mock(GestureCallback.class);
        match = new Match(MatchType.BO1, GameType.G11, ServeRules.S2, new Player(PLAYER_1_NAME), new Player(PLAYER_2_NAME), uiCallback, startingServerSide, gestureCallback);
    }

    @After
    public void tearDown() {
        match = null;
        uiCallback = null;
        gestureCallback = null;
    }

    @Test
    public void testRestartOfAMatch() {
        match.restartMatch();
        assertEquals(startingServerSide, match.getReferee().getServer());

        // check server side on restart matches in different scenarios
        // Scenario 1: One Player still ends the match with a serve left
        for (int i = 0; i<11; i++) {
            match.getReferee().onPointAddition(Side.LEFT);
        }
        checkStartingServerAfterAMatchRestart();

        // Scenario 2: One Player still ends the match with no serve left
        match.getReferee().onPointAddition(Side.RIGHT);
        for (int i = 0; i<11; i++) {
            match.getReferee().onPointAddition(Side.LEFT);
        }
        checkStartingServerAfterAMatchRestart();

        // Scenario 3: Same as Scenario 1 but for other player
        match.getReferee().onPointAddition(Side.RIGHT);
        match.getReferee().onPointAddition(Side.RIGHT);
        for (int i = 0; i<11; i++) {
            match.getReferee().onPointAddition(Side.LEFT);
        }
        checkStartingServerAfterAMatchRestart();

        // Scenario 4: Same as Scenario 2 but for other player
        match.getReferee().onPointAddition(Side.RIGHT);
        match.getReferee().onPointAddition(Side.RIGHT);
        match.getReferee().onPointAddition(Side.RIGHT);
        for (int i = 0; i<11; i++) {
            match.getReferee().onPointAddition(Side.LEFT);
        }
        checkStartingServerAfterAMatchRestart();
    }

    private void checkStartingServerAfterAMatchRestart() {
        match.restartMatch();
        assertEquals(startingServerSide, match.getReferee().getServer());
        match.getReferee().onPointAddition(Side.LEFT);
        assertEquals(startingServerSide, match.getReferee().getServer());
        match.getReferee().onPointAddition(Side.LEFT);
        assertEquals(Side.getOpposite(startingServerSide), match.getReferee().getServer());
        match.restartMatch();
    }

    @Test
    public void onWinInBO1Match() {
        match = spy(match);
        assertNotNull(match.getReferee());
        match.onWin(Side.RIGHT);
        verify(uiCallback, times(1)).onMatchEnded(PLAYER_2_NAME);
        verify(uiCallback, times(1)).onWin(Side.RIGHT, 1);
        verify(match, times(1)).end(Side.RIGHT);
    }

    @Test
    public void onWinInBO3Match() {
        testWithMatchType(MatchType.BO3);
    }

    @Test
    public void onWinInBO5Match() {
        testWithMatchType(MatchType.BO5);
    }

    private void testWithMatchType(MatchType type) {
        match = new Match(type, GameType.G11, ServeRules.S2, new Player(PLAYER_1_NAME), new Player(PLAYER_2_NAME), uiCallback, Side.LEFT, gestureCallback);
        int winsToEnd = type.gamesNeededToWin;
        for(int i = 0; i<winsToEnd-1; i++) {
            match.onWin(Side.LEFT);
            match.onWin(Side.RIGHT);
            verify(uiCallback, times(1)).onWin(Side.LEFT, i+1);
            verify(uiCallback, times(1)).onWin(Side.RIGHT, i+1);
            verify(uiCallback, times(0)).onMatchEnded(PLAYER_1_NAME);
            verify(uiCallback, times(0)).onMatchEnded(PLAYER_2_NAME);
        }
        // finally let one side win
        match.onWin(Side.RIGHT);
        verify(uiCallback, times(1)).onWin(Side.RIGHT, winsToEnd);
        verify(uiCallback, times(1)).onMatchEnded(PLAYER_2_NAME);
    }
}