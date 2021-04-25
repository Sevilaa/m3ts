package ch.m3ts.tabletennis.match.game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.Subscribable;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.data.game.GameEventData;
import ch.m3ts.eventbus.data.todisplay.ToDisplayData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import ch.m3ts.tabletennis.match.GameListener;
import ch.m3ts.tabletennis.match.ServeRules;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class StubListenerGame implements Subscribable {
    private final GameListener gameListener;
    private final DisplayUpdateListener displayUpdateListener;

    StubListenerGame(DisplayUpdateListener displayUpdateListener, GameListener gameListener) {
        this.displayUpdateListener = displayUpdateListener;
        this.gameListener = gameListener;
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof GameEventData) {
            GameEventData gameEventData = (GameEventData) data;
            gameEventData.call(gameListener);
        } else if (data instanceof ToDisplayData) {
            ToDisplayData toDisplayData = (ToDisplayData) data;
            toDisplayData.call(displayUpdateListener);
        }
    }
}

public class GameTest {
    private GameListener gameListener;
    private DisplayUpdateListener displayUpdateListener;
    private Game game;
    private final Side STARTING_SIDE = Side.LEFT;
    private StubListenerGame stubListenerGame;

    @Before
    public void setUp() {
        gameListener = mock(ch.m3ts.tabletennis.match.GameListener.class);
        displayUpdateListener = mock(DisplayUpdateListener.class);
        stubListenerGame = new StubListenerGame(displayUpdateListener, gameListener);
        TTEventBus.getInstance().register(stubListenerGame);
        game = new Game(GameType.G11, ServeRules.S2, STARTING_SIDE);
    }

    @After
    public void cleanUp() {
        TTEventBus.getInstance().unregister(stubListenerGame);
    }

    @Test
    public void testOnPointDeduction() {
        game = new Game(GameType.G11, ServeRules.S2, STARTING_SIDE);
        game.onPointDeduction(Side.RIGHT);
        game.onPointDeduction(Side.LEFT);
        verify(displayUpdateListener, never()).onScore(any(Side.class), anyInt(), any(Side.class), any(Side.class));
        verify(displayUpdateListener, never()).onWin(any(Side.class), anyInt());
        assertEquals(0, game.getScore(Side.RIGHT));
        assertEquals(0, game.getScore(Side.LEFT));
        game.onPoint(Side.RIGHT);
        game.onPoint(Side.LEFT);
        game.onPoint(Side.RIGHT);
        assertEquals(2, game.getScore(Side.RIGHT));
        game.onPointDeduction(Side.RIGHT);
        game.onPointDeduction(Side.RIGHT);
        assertEquals(0, game.getScore(Side.RIGHT));
        game.onPointDeduction(Side.LEFT);
        game.onPointDeduction(Side.LEFT);
        assertEquals(0, game.getScore(Side.LEFT));
        // expect 6 times on score -> 3 for onPoint, 3 for onPointDeduction
        verify(displayUpdateListener, times(6)).onScore(any(Side.class), anyInt(), any(Side.class), any(Side.class));
    }

    @Test
    public void testServerOnPointManipulation() {
        assertEquals(STARTING_SIDE, game.getServer());
        game.onPoint(Side.RIGHT);
        assertEquals(STARTING_SIDE, game.getServer());
        game.onPoint(Side.RIGHT);
        assertEquals(Side.RIGHT, game.getServer());
        // manipulate ("actually, LEFT scored instead of RIGHT")
        game.onPointDeduction(Side.RIGHT);
        assertEquals(STARTING_SIDE, game.getServer());
        game.onPoint(Side.LEFT);
        assertEquals(Side.RIGHT, game.getServer());
        game.onPoint(Side.LEFT);
        assertEquals(Side.RIGHT, game.getServer());
        game.onPoint(Side.LEFT);
        assertEquals(Side.LEFT, game.getServer());
        game.onPoint(Side.RIGHT);
        assertEquals(Side.LEFT, game.getServer());
        // R:2 L:3 -> revert one for edge case score % 2 == 0
        game.onPointDeduction(Side.LEFT);
        assertEquals(Side.LEFT, game.getServer());
        // R:2 L:2 -> now revert two more
        game.onPointDeduction(Side.RIGHT);
        assertEquals(Side.RIGHT, game.getServer());
        game.onPointDeduction(Side.LEFT);
        assertEquals(Side.RIGHT, game.getServer());
        // R:1 L:1 -> now revert back to zero
        game.onPointDeduction(Side.RIGHT);
        assertEquals(Side.LEFT, game.getServer());
        game.onPointDeduction(Side.LEFT);
        assertEquals(Side.LEFT, game.getServer());
    }

    @Test
    public void onWin() {
        // let left side win 11:0
        for (int i = 0; i<GameType.G11.amountOfPoints; i++) {
            game.onPoint(Side.LEFT);
            verify(displayUpdateListener, times(1)).onScore(eq(Side.LEFT), eq(i + 1), any(Side.class), any(Side.class));
        }
        verify(gameListener, times(1)).onGameWin(Side.LEFT);

        // let the right side win 11:0
        game = new Game(GameType.G11, ServeRules.S2, STARTING_SIDE);
        for (int i = 0; i<11; i++) {
            game.onPoint(Side.RIGHT);
            verify(displayUpdateListener, times(1)).onScore(eq(Side.RIGHT), eq(i + 1), any(Side.class), any(Side.class));
        }
        verify(gameListener, times(1)).onGameWin(Side.RIGHT);
    }

    @Test
    public void noWinsCalled() {
        for (int i = 0; i < 11; i++) {
            // if only 11 points get played and the score is not 11:0 -> no win
            if (i < 6) {
                game.onPoint(Side.RIGHT);
            } else {
                game.onPoint(Side.LEFT);
            }
        }
        verify(gameListener, times(0)).onGameWin(Side.RIGHT);
        verify(gameListener, times(0)).onGameWin(Side.LEFT);

        game = new Game(GameType.G11, ServeRules.S2, STARTING_SIDE);
        for (int i = 0; i < 20; i++) {
            if (i < 10) {
                game.onPoint(Side.RIGHT);
            } else {
                game.onPoint(Side.LEFT);
            }
        }
        game.onPoint(Side.RIGHT);
        game.onPoint(Side.LEFT);
        // score is now 11:11 -> no onWin called (overtime)
        verify(gameListener, times(0)).onGameWin(Side.RIGHT);
        verify(gameListener, times(0)).onGameWin(Side.LEFT);

        for (int i = 0; i < 100; i++) {
            // overtime can go endless..
            Side side = Side.RIGHT;
            if (i % 2 == 0) {
                side = Side.LEFT;
            }
            game.onPoint(side);
        }
        verify(gameListener, times(0)).onGameWin(Side.RIGHT);
        verify(gameListener, times(0)).onGameWin(Side.LEFT);

        // finally make end the game by having one score + 2p higher
        game.onPoint(Side.RIGHT);
        game.onPoint(Side.RIGHT);
        verify(gameListener, times(1)).onGameWin(Side.RIGHT);
        verify(gameListener, times(0)).onGameWin(Side.LEFT);
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
        game = new Game(GameType.G11, ServeRules.S5, STARTING_SIDE);
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
        game = new Game(GameType.G11, ServeRules.S5, STARTING_SIDE);
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