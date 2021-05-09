package ch.m3ts.tracker.visualization.replay.benchmark;

import ch.m3ts.tabletennis.helper.Side;

public class BenchmarkClipManager {
    private static final String SCORE_DIVIDING_SYMBOL = "_";
    private static final String VIDEO_MEDIA_TYPE = ".mp4";
    private final String[][] clips;
    private final String[] sets;
    private int currentTestSet;
    private int currentClip;

    public BenchmarkClipManager(String[][] clips, String[] sets) {
        if (clips.length != sets.length)
            throw new RuntimeException("Amount of clips do not match test sets!");
        this.sets = sets;
        this.clips = clips;
    }

    public int getCurrentTestSetId() {
        return currentTestSet;
    }

    public String getCurrentTestSet() {
        return sets[currentTestSet];
    }

    public String getCurrentClip() {
        return clips[currentTestSet][currentClip];
    }

    public String[] getSets() {
        return sets;
    }

    public boolean advanceToNextClip() {
        if (currentClip >= clips[currentTestSet].length - 1) {
            return false;
        } else {
            currentClip++;
            return true;
        }
    }

    public boolean advanceToNextTestSet() {
        if (currentTestSet >= sets.length - 1) {
            return false;
        } else {
            this.currentClip = 0;
            currentTestSet++;
            return true;
        }
    }

    public Side readWhichSideShouldScore() {
        Side sideToScore;
        String currentClipName = getCurrentClip();
        currentClipName = currentClipName.split(VIDEO_MEDIA_TYPE)[0];
        String[] currentScoresAsString = currentClipName.split(SCORE_DIVIDING_SYMBOL);
        int[] currentScores = {
                Integer.parseInt(currentScoresAsString[0]), Integer.parseInt(currentScoresAsString[1])
        };

        if (currentClip == 0) {
            if (currentScores[0] > currentScores[1]) {
                sideToScore = Side.LEFT;
            } else {
                sideToScore = Side.RIGHT;
            }
        } else {
            String lastClipName = clips[currentTestSet][currentClip - 1].split(VIDEO_MEDIA_TYPE)[0];
            String[] lastScoresAsString = lastClipName.split(SCORE_DIVIDING_SYMBOL);
            int[] lastScores = {
                    Integer.parseInt(lastScoresAsString[0]), Integer.parseInt(lastScoresAsString[1])
            };
            if (currentScores[0] > lastScores[0]) {
                sideToScore = Side.LEFT;
            } else {
                sideToScore = Side.RIGHT;
            }
        }
        return sideToScore;
    }
}
