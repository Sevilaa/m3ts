package cz.fmo.tabletennis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MatchTest {
    private Match match;
    private UICallback uiCallback;
    private final String PLAYER_1_NAME = "Testian";
    private final String PLAYER_2_NAME = "Mockian";

    @Before
    public void setUp() {
        uiCallback = mock(UICallback.class);
        match = new Match(MatchType.BO1, GameType.G11, ServeRules.S2, PLAYER_1_NAME, PLAYER_2_NAME, uiCallback, Side.LEFT);
    }

    @After
    public void tearDown() {
        match = null;
        uiCallback = null;
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
        match = new Match(type, GameType.G11, ServeRules.S2, PLAYER_1_NAME, PLAYER_2_NAME, uiCallback, Side.LEFT);
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