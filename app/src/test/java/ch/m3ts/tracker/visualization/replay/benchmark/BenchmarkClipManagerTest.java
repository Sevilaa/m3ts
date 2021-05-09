package ch.m3ts.tracker.visualization.replay.benchmark;

import org.junit.Before;
import org.junit.Test;

import ch.m3ts.tabletennis.helper.Side;

import static org.junit.Assert.assertEquals;

public class BenchmarkClipManagerTest {
    private BenchmarkClipManager clipManager;

    private String[] testSets = {
            "set_1", "other_set"
    };

    private String[][] clips = {
            {"1_0.mp4", "1_1.mp4", "1_2.mp4"},
            {"00_01.mp4", "00_02.mp4", "00_03.mp4", "01_03.mp4"}
    };

    private Side[][] winners = {
            {Side.LEFT, Side.RIGHT, Side.RIGHT},
            {Side.RIGHT, Side.RIGHT, Side.RIGHT, Side.LEFT}
    };

    @Before
    public void init() {
        clipManager = new BenchmarkClipManager(clips, testSets);
    }

    @Test
    public void testWinnerSide() {
        for (int i = 0; i < testSets.length; i++) {
            for (int j = 0; j < clips[i].length; j++) {
                assertEquals(winners[i][j], clipManager.readWhichSideShouldScore());
                if (!clipManager.advanceToNextClip()) {
                    clipManager.advanceToNextTestSet();
                }
            }
        }
    }

    @Test
    public void testAdvance() {
        for (int i = 0; i < testSets.length; i++) {
            assertEquals(testSets[i], clipManager.getCurrentTestSet());
            assertEquals(i, clipManager.getCurrentTestSetId());
            for (int j = 0; j < clips[i].length; j++) {
                assertEquals(clips[i][j], clipManager.getCurrentClip());
                if (!clipManager.advanceToNextClip()) {
                    clipManager.advanceToNextTestSet();
                }
            }
        }
    }

    @Test(expected = RuntimeException.class)
    public void initWithInvalidData() {
        clipManager = new BenchmarkClipManager(new String[][]{{"1_0.mp4"}, {"1_0.mp4"}}, new String[]{"one_set"});
    }

}