package cz.fmo.tabletennis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GameTest {
    private MatchCallback matchCallback;
    private UICallback uiCallback;
    private Game game;
    // TODO change to dynamic implementation
    private final Side STARTING_SIDE = Side.LEFT;

    @Before
    public void setUp() {
        matchCallback = mock(MatchCallback.class);
        uiCallback = mock(UICallback.class);
        game = new Game(matchCallback, uiCallback, GameType.G11, ServeRules.S2, STARTING_SIDE);
    }

    @After
    public void tearDown() {
        game = null;
        matchCallback = null;
        uiCallback = null;
    }

    @Test
    public void onWin() {
        // let left side win 11:0
        for (int i = 0; i<GameType.G11.amountOfPoints; i++) {
            game.onPoint(Side.LEFT);
            verify(uiCallback, times(1)).onScore(Side.LEFT, i+1);
        }
        verify(matchCallback, times(1)).onWin(Side.LEFT);

        // let the right side win 11:0
        game = new Game(matchCallback, uiCallback, GameType.G11, ServeRules.S2, STARTING_SIDE);
        for (int i = 0; i<11; i++) {
            game.onPoint(Side.RIGHT);
            verify(uiCallback, times(1)).onScore(Side.RIGHT, i+1);
        }
        verify(matchCallback, times(1)).onWin(Side.RIGHT);
    }

    @Test
    public void noWinsCalled() {
        for (int i = 0; i<11; i++) {
            // if only 11 points get played and the score is not 11:0 -> no win
            if (i<6) {
                game.onPoint(Side.RIGHT);
            } else {
                game.onPoint(Side.LEFT);
            }
        }
        verify(matchCallback, times(0)).onWin(Side.RIGHT);
        verify(matchCallback, times(0)).onWin(Side.LEFT);

        game = new Game(matchCallback, uiCallback, GameType.G11, ServeRules.S2, STARTING_SIDE);
        for (int i = 0; i<20; i++) {
            if (i<10) {
                game.onPoint(Side.RIGHT);
            } else {
                game.onPoint(Side.LEFT);
            }
        }
        game.onPoint(Side.RIGHT);
        game.onPoint(Side.LEFT);
        // score is now 11:11 -> no onWin called (overtime)
        verify(matchCallback, times(0)).onWin(Side.RIGHT);
        verify(matchCallback, times(0)).onWin(Side.LEFT);

        for(int i = 0; i<100; i++) {
            // overtime can go endless..
            Side side = Side.RIGHT;
            if(i % 2 == 0) {
                side = Side.LEFT;
            }
            game.onPoint(side);
        }
        verify(matchCallback, times(0)).onWin(Side.RIGHT);
        verify(matchCallback, times(0)).onWin(Side.LEFT);

        // finally make end the game by having one score + 2p higher
        game.onPoint(Side.RIGHT);
        game.onPoint(Side.RIGHT);
        verify(matchCallback, times(1)).onWin(Side.RIGHT);
        verify(matchCallback, times(0)).onWin(Side.LEFT);
    }

    @Test
    public void changeServerWithServeRuleS2() {
        Side currentServer = STARTING_SIDE;
        for(int i = 1; i < 6; i++) {
            assertEquals(currentServer, game.getServer());
            game.onPoint(Side.LEFT);
            if(i%2 == 0) {
                currentServer = switchSide(currentServer);
            }
        }
        assertEquals(currentServer, game.getServer());
    }

    @Test
    public void changeServerWithServeRuleS5() {
        game = new Game(matchCallback, uiCallback, GameType.G11, ServeRules.S5, STARTING_SIDE);
        Side currentServer = STARTING_SIDE;
        for(int i = 0; i < 2; i++) {
            for(int j = 1; j < 6; j++) {
                assertEquals(currentServer, game.getServer());
                game.onPoint(Side.LEFT);
                if(j%5 == 0) {
                    currentServer = switchSide(currentServer);
                }
            }
        }
        assertEquals(currentServer, game.getServer());
    }

    @Test
    public void changeServerServeRuleS2OverTime() {
        Side currentServer = STARTING_SIDE;
        for(int i = 0; i < GameType.G11.amountOfPoints-1; i++) {
            game.onPoint(Side.LEFT);
            game.onPoint(Side.RIGHT);
        }
        assertEquals(currentServer, game.getServer());
        for(int i = 0; i < 4; i++) {
            game.onPoint(Side.LEFT);
            assertEquals(switchSide(currentServer), game.getServer());
            game.onPoint(Side.RIGHT);
            assertEquals(currentServer, game.getServer());
        }
    }

    @Test
    public void changeServerServeRuleS5OverTime() {
        Side currentServer = STARTING_SIDE;
        game = new Game(matchCallback, uiCallback, GameType.G11, ServeRules.S5, STARTING_SIDE);
        for(int i = 0; i < GameType.G11.amountOfPoints-1; i++) {
            game.onPoint(Side.LEFT);
            game.onPoint(Side.RIGHT);
        }
        assertEquals(currentServer, game.getServer());
        for(int i = 0; i < 4; i++) {
            game.onPoint(Side.LEFT);
            assertEquals(switchSide(currentServer), game.getServer());
            game.onPoint(Side.RIGHT);
            assertEquals(currentServer, game.getServer());
        }
    }

    private Side switchSide(Side side) {
        if(side == Side.LEFT) {
            return Side.RIGHT;
        } else {
            return Side.LEFT;
        }
    }
}