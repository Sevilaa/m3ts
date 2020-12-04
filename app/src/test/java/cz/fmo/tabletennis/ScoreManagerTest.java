package cz.fmo.tabletennis;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ScoreManagerTest {
    private ScoreManager scoreManager;
    private Random random;

    @Before
    public void init() {
        random = new Random();
        scoreManager = new ScoreManager(Side.LEFT);
    }

    @Test
    public void testRevertOnZero() {
        for(int i = 0; i<random.nextInt(100)+10; i++) {
            Side lastServer = scoreManager.revertLastScore(Side.RIGHT);
            assertEquals(Side.LEFT, lastServer);
            lastServer = scoreManager.revertLastScore(Side.LEFT);
            assertEquals(Side.LEFT, lastServer);
            assertEquals(0, scoreManager.getScore(Side.RIGHT));
            assertEquals(0, scoreManager.getScore(Side.LEFT));
        }
    }

    @Test
    public void testRevert() {
        assertEquals(0, scoreManager.getScore(Side.RIGHT));
        assertEquals(0, scoreManager.getScore(Side.LEFT));

        scoreManager.score(Side.RIGHT, Side.LEFT);
        scoreManager.score(Side.RIGHT, Side.LEFT);
        scoreManager.score(Side.LEFT, Side.RIGHT);
        scoreManager.score(Side.LEFT, Side.RIGHT);
        assertEquals(2, scoreManager.getScore(Side.RIGHT));
        assertEquals(2, scoreManager.getScore(Side.LEFT));

        Side lastServer = scoreManager.revertLastScore(Side.LEFT);
        assertEquals(Side.RIGHT, lastServer);
        assertEquals(2, scoreManager.getScore(Side.RIGHT));
        assertEquals(1, scoreManager.getScore(Side.LEFT));

        lastServer = scoreManager.revertLastScore(Side.LEFT);
        assertEquals(Side.RIGHT, lastServer);
        assertEquals(2, scoreManager.getScore(Side.RIGHT));
        assertEquals(0, scoreManager.getScore(Side.LEFT));

        lastServer = scoreManager.revertLastScore(Side.RIGHT);
        assertEquals(Side.LEFT, lastServer);
        assertEquals(1, scoreManager.getScore(Side.RIGHT));
        assertEquals(0, scoreManager.getScore(Side.LEFT));

        lastServer = scoreManager.revertLastScore(Side.RIGHT);
        assertEquals(Side.LEFT, lastServer);
        assertEquals(0, scoreManager.getScore(Side.RIGHT));
        assertEquals(0, scoreManager.getScore(Side.LEFT));
    }
}