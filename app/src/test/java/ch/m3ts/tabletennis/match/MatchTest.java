package ch.m3ts.tabletennis.match;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.m3ts.event.Event;
import ch.m3ts.event.EventBus;
import ch.m3ts.event.Subscribable;
import ch.m3ts.event.TTEvent;
import ch.m3ts.event.TTEventBus;
import ch.m3ts.event.data.game.GameWinData;
import ch.m3ts.event.data.game.GameWinResetData;
import ch.m3ts.event.data.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.game.GameType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class StubListenerMatch implements Subscribable {
    private final DisplayUpdateListener displayUpdateListener;

    StubListenerMatch(DisplayUpdateListener displayUpdateListener) {
        this.displayUpdateListener = displayUpdateListener;
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof ToDisplayData) {
            ToDisplayData toDisplayData = (ToDisplayData) data;
            toDisplayData.call(displayUpdateListener);
        }
    }
}

public class MatchTest {
    private Match match;
    private DisplayUpdateListener displayUpdateListener;
    private final String PLAYER_1_NAME = "Test_Spieler";
    private final String PLAYER_2_NAME = "Test_Spieler_Der_Zweite";
    private final Side startingServerSide = Side.LEFT;
    private StubListenerMatch stubListenerMatch;

    @Before
    public void setUp() {
        displayUpdateListener = mock(DisplayUpdateListener.class);
        stubListenerMatch = new StubListenerMatch(displayUpdateListener);
        TTEventBus.getInstance().register(stubListenerMatch);
        match = new Match(MatchType.BO1, GameType.G11, ServeRules.S2, new Player(PLAYER_1_NAME), new Player(PLAYER_2_NAME), startingServerSide);
    }

    @After
    public void cleanUp() {
        TTEventBus.getInstance().unregister(stubListenerMatch);
    }

    @Test
    public void testHandlingEvents() {
        match = spy(match);
        EventBus eventBus = TTEventBus.getInstance();
        eventBus.register(match);
        eventBus.dispatch(new TTEvent<>(new GameWinResetData()));
        eventBus.dispatch(new TTEvent<>(new GameWinData(Side.LEFT)));
        verify(match, times(1)).onGameWinReset();
        verify(match, times(1)).onGameWin(Side.LEFT);
        eventBus.unregister(match);
    }

    @Test
    public void testGameWinReset() {
        match = new Match(MatchType.BO1, GameType.G11, ServeRules.S2, new Player(PLAYER_1_NAME), new Player(PLAYER_2_NAME), startingServerSide);
        TTEventBus.getInstance().register(match);
        for (int i = 0; i < 11; i++) {
            match.getReferee().onPointAddition(Side.RIGHT);
        }
        MatchStatus status = match.onRequestMatchStatus();
        assertEquals(0, status.getWinsLeft());
        assertEquals(1, status.getWinsRight());
        assertEquals(11, status.getScoreRight());
        assertEquals(0, status.getScoreLeft());

        match.onGameWinReset();
        status = match.onRequestMatchStatus();
        assertEquals(0, status.getWinsLeft());
        assertEquals(0, status.getWinsRight());
        assertEquals(10, status.getScoreRight());
        assertEquals(0, status.getScoreLeft());
        TTEventBus.getInstance().unregister(match);

        // same test but in BO3 instead of BO1
        match = new Match(MatchType.BO3, GameType.G11, ServeRules.S2, new Player(PLAYER_1_NAME), new Player(PLAYER_2_NAME), startingServerSide);
        TTEventBus.getInstance().register(match);
        for (int i = 0; i < 11; i++) {
            match.getReferee().onPointAddition(Side.LEFT);
        }
        status = match.onRequestMatchStatus();
        assertEquals(1, status.getWinsLeft());
        assertEquals(0, status.getWinsRight());
        assertEquals(0, status.getScoreRight());
        assertEquals(0, status.getScoreLeft());
        match.onGameWinReset();
        status = match.onRequestMatchStatus();
        assertEquals(0, status.getWinsLeft());
        assertEquals(0, status.getWinsRight());
        assertEquals(0, status.getScoreRight());
        assertEquals(10, status.getScoreLeft());
    }

    @Test
    public void testRestartOfAMatch() {
        match.restartMatch();
        assertEquals(startingServerSide, match.getReferee().getServer());

        // check server side on restart matches in different scenarios
        // Scenario 1: One Player still ends the match with a serve left
        for (int i = 0; i < 11; i++) {
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
        match.onGameWin(Side.RIGHT);
        verify(displayUpdateListener, times(1)).onMatchEnded(PLAYER_2_NAME);
        verify(displayUpdateListener, times(0)).onWin(Side.RIGHT, 1);
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
        match = new Match(type, GameType.G11, ServeRules.S2, new Player(PLAYER_1_NAME), new Player(PLAYER_2_NAME), Side.LEFT);
        int winsToEnd = type.gamesNeededToWin;
        for (int i = 0; i < winsToEnd - 1; i++) {
            match.onGameWin(Side.LEFT);
            match.onGameWin(Side.RIGHT);
            verify(displayUpdateListener, times(1)).onWin(Side.LEFT, i + 1);
            verify(displayUpdateListener, times(1)).onWin(Side.RIGHT, i + 1);
            verify(displayUpdateListener, times(0)).onMatchEnded(PLAYER_1_NAME);
            verify(displayUpdateListener, times(0)).onMatchEnded(PLAYER_2_NAME);
        }
        // finally let one side win
        match.onGameWin(Side.RIGHT);
        verify(displayUpdateListener, times(0)).onWin(Side.RIGHT, winsToEnd);
        verify(displayUpdateListener, times(1)).onMatchEnded(PLAYER_2_NAME);
    }
}