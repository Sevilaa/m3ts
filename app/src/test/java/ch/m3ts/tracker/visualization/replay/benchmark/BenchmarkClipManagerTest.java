package ch.m3ts.tracker.visualization.replay.benchmark;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.m3ts.tabletennis.helper.Side;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

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

    @Test
    public void testMakeStatistics() {
        String[] someSets = {"set_uno", "other_set"};
        int[] totalJudgements = {10, 30};
        int[] correctJudgements = {10, 15};
        BenchmarkClipManager clipManager = Mockito.mock(BenchmarkClipManager.class);
        Mockito.when(clipManager.getSets()).thenReturn(someSets);
        String stats = BenchmarkClipManager.makeStatisticsString(totalJudgements, correctJudgements, clipManager);
        Mockito.verify(clipManager, times(1)).getSets();
        assertTrue(stats.contains(someSets[0]));
        assertTrue(stats.contains(someSets[1]));
        assertTrue(stats.contains(String.valueOf(totalJudgements[0])));
        assertTrue(stats.contains(String.valueOf(totalJudgements[1])));
        assertTrue(stats.contains(String.valueOf(correctJudgements[0])));
        assertTrue(stats.contains(String.valueOf(correctJudgements[1])));
        assertTrue(stats.contains("100.0%"));
        assertTrue(stats.contains("50.0%"));
    }

    @Test(expected = RuntimeException.class)
    public void initWithInvalidData() {
        clipManager = new BenchmarkClipManager(new String[][]{{"1_0.mp4"}, {"1_0.mp4"}}, new String[]{"one_set"});
    }

}