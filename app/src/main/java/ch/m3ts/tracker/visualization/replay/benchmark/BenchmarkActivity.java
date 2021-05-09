/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.m3ts.tracker.visualization.replay.benchmark;

import android.opengl.GLES20;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import ch.m3ts.tracker.visualization.replay.lib.SpeedControlCallback;
import ch.m3ts.tracker.visualization.replay.lib.VideoPlayer;
import ch.m3ts.util.Log;
import cz.fmo.R;
import cz.fmo.graphics.EGL;
import cz.fmo.util.Config;
import cz.fmo.util.FileManager;

/**
 * Activity which loads video snippets of played matches and benchmarks them in terms of how good
 * the virtual Referee is working.
 **/
@SuppressWarnings("squid:S110")
public class BenchmarkActivity extends MatchVisualizeActivity implements VideoPlayer.PlayerFeedback, View.OnClickListener {
    private static final double VIEWING_ANGLE_HORIZONTAL = 66.56780242919922; // viewing angle of phone which we used for recordings
    private static final String BENCHMARK_PREFIX = "!test_";
    private static final String SCORE_DIVIDING_SYMBOL = "_";
    private static final String VIDEO_MEDIA_TYPE = ".mp4";
    private String[] mTestSets;
    private FileManager[] mFileManagers;
    private String[] mTestSetClips;
    private int[] nTotalJudgements;
    private int[] nCorrectJudgements;
    private int currentClip;
    private int currentTestSet;
    private boolean mShowStopLabel;
    private boolean doCancelBenchmark;
    private VideoPlayer.PlayTask mPlayTask;
    private BenchmarkHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHandler = new BenchmarkHandler(this);
        this.currentTestSet = 0;
        this.currentClip = 0;
        this.mTestSets = getResources().getStringArray(R.array.testSets);
        this.mFileManagers = new FileManager[mTestSets.length];
        this.nCorrectJudgements = new int[mTestSets.length];
        this.nTotalJudgements = new int[mTestSets.length];
        this.doCancelBenchmark = false;

        for (int i = 0; i < mTestSets.length; i++) {
            this.mFileManagers[i] = new FileManager(this, mTestSets[i]);
            this.nTotalJudgements[i] = mFileManagers[i].listMP4().length;
        }

        Button playStopButton = findViewById(R.id.benchmark_start_button);
        playStopButton.setOnClickListener(this);
        updateControls();
        setCurrentClips();
    }

    /**
     * onClick handler of "start benchmark" button
     */
    @Override
    public void onClick(View view) {
        initCurrentTestSet();
        setWhoShouldScore();
        playCurrentClip();
    }

    @Override
    public void onBackPressed() {
        doCancelBenchmark = true;
        super.onBackPressed();
    }

    private void setCurrentClips() {
        mTestSetClips = mFileManagers[currentTestSet].listMP4();
    }

    private FileManager getCurrentFileManager() {
        return mFileManagers[currentTestSet];
    }

    @Override
    public double getCameraHorizontalViewAngle() {
        return VIEWING_ANGLE_HORIZONTAL;
    }

    @Override
    public void init() {
        updateControls();
    }

    @Override
    public void setCurrentContentView() {
        setContentView(R.layout.activity_benchmark);
    }

    /**
     * Gets called when the clip (.mp4) is done
     */
    @Override
    public void playbackStopped() {
        mHandler.onClipEnded();
        mShowStopLabel = false;
        mPlayTask = null;
        if (!doCancelBenchmark) {
            if (!advanceToNextClip() && !advanceToNextTestSet()) {
                finishBenchmark();
                return;
            }
            setWhoShouldScore();
            playCurrentClip();
        }
    }

    private void setWhoShouldScore() {
        Side sideToScore;
        String currentClipName = mTestSetClips[currentClip];
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
            String lastClipName = mTestSetClips[currentClip - 1].split(VIDEO_MEDIA_TYPE)[0];
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
        mHandler.setWhoShouldScore(sideToScore);
    }

    private boolean advanceToNextTestSet() {
        this.finishCurrentTestSet();
        if (currentTestSet >= mTestSets.length - 1) {
            return false;
        } else {
            this.currentClip = 0;
            currentTestSet++;
            setCurrentClips();
            this.initCurrentTestSet();
            return true;
        }
    }

    private void finishBenchmark() {
        this.onPause();
        updateControls();
        printStatistics();
        Toast.makeText(this, R.string.benchmark_finished_toast_text, Toast.LENGTH_LONG).show();
    }

    private void printStatistics() {
        int allJudgements = 0;
        for (int j : this.nTotalJudgements) {
            allJudgements += j;
        }

        if (allJudgements == 0) allJudgements = 1;

        int allCorrectJudgements = 0;
        for (int c : this.nCorrectJudgements) {
            allCorrectJudgements += c;
        }

        Log.d("-------------------- BENCHMARK DONE --------------------");
        Log.d(String.format(Locale.US, "%-45s%d", "Total amount of Judgements:", allJudgements));
        Log.d(String.format(Locale.US, "%-45s%d", "Total amount of correct Judgements:", allCorrectJudgements));
        Log.d(String.format(Locale.US, "%-45s%.1f%%", "In percentage:", ((double) allCorrectJudgements / allJudgements) * 100));
        Log.d("Stats per test set =>");
        for (int i = 0; i < mTestSets.length; i++) {
            String testSet = mTestSets[i];
            String formattedTestSetString = String.format(Locale.US, "set '%s':", testSet);
            Log.d(String.format(Locale.US, "%-38s%d/%d => %.1f%%", formattedTestSetString, nCorrectJudgements[i], nTotalJudgements[i],
                    ((double) nCorrectJudgements[i] / nTotalJudgements[i]) * 100));
        }
        Log.d("--------------------------------------------------------");
    }

    private boolean advanceToNextClip() {
        if (currentClip >= mTestSetClips.length - 1) {
            return false;
        } else {
            currentClip++;
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mHandler.onResumeActivity();
    }

    @Override
    protected void onPause() {
        this.mHandler.onPauseActivity();
        super.onPause();
        // We're not keeping track of the state in static fields, so we need to shut the
        // playback down.  Ideally we'd preserve the state so that the player would continue
        // after a device rotation.
        //
        // We want to be sure that the player won't continue to send frames after we pause,
        // because we're tearing the view down.  So we wait for it to stop here.
        if (mPlayTask != null) {
            mPlayTask.requestStop();
            mPlayTask.waitForStop();
        }
        //here
        mHandler.stopDetections();
    }

    /**
     * Updates the on-screen controls to reflect the current state of the app.
     */
    private void updateControls() {
        Button play = findViewById(R.id.benchmark_start_button);
        if (mShowStopLabel) {
            play.setText(R.string.benchmark_stop_button_text);
            play.setEnabled(false);
            play.setClickable(false);
        } else {
            play.setText(R.string.benchmark_play_button_text);
            play.setEnabled(true);
            play.setClickable(true);
        }
        play.setEnabled(ismSurfaceHolderReady());
    }

    /**
     * Clears the playback surface to black.
     */
    private void clearSurface(Surface surface) {
        // We need to do this with OpenGL ES (*not* Canvas -- the "software render" bits
        // are sticky).  We can't stay connected to the Surface after we're done because
        // that'd prevent the video encoder from attaching.
        //
        // If the Surface is resized to be larger, the new portions will be black, so
        // clearing to something other than black may look weird unless we do the clear
        // post-resize.
        EGL egl = new EGL();
        EGL.Surface win = egl.makeSurface(surface);
        win.makeCurrent();
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        win.swapBuffers();
        win.release();
        egl.release();
    }

    /**
     * Tries to load the table location from an xml file from assets.
     *
     * @param videoFileName - Full name of video file in phones Camera dir. Example: "bounce_back_1.mp4"
     */
    private Table trySettingTableLocationFromXML(String videoFileName) {
        String fileNameWithoutExtension = videoFileName.split("\\.")[0];
        try (InputStream is = getAssets().open(fileNameWithoutExtension + ".xml")) {
            Properties properties = new Properties();
            properties.loadFromXML(is);
            return Table.makeTableFromProperties(properties);
        } catch (IOException ex) {
            Log.e(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Tries to load the serving side from an xml file from assets.
     *
     * @param videoFileName - Full name of video file in phones Camera dir. Example: "bounce_back_1.mp4"
     */
    private Side tryGettingServingSideFromXML(String videoFileName) {
        Side servingSide = Side.LEFT;
        String fileNameWithoutExtension = videoFileName.split("\\.")[0];
        try (InputStream is = getAssets().open(fileNameWithoutExtension + ".xml")) {
            Properties properties = new Properties();
            properties.loadFromXML(is);
            if (properties.containsKey("servingSide") && properties.getProperty("servingSide").equals("RIGHT"))
                servingSide = Side.RIGHT;
        } catch (IOException ex) {
            Log.e(ex.getMessage(), ex);
        }
        return servingSide;
    }

    private void finishCurrentTestSet() {
        this.nCorrectJudgements[currentTestSet] = mHandler.getCorrectJudgementCalls();
        mHandler.stopDetections();
        mHandler.onPauseActivity();
    }

    private void initCurrentTestSet() {
        if (mPlayTask != null) {
            Log.w("movie already playing");
            return;
        }
        this.mHandler.onResumeActivity();
        Log.d("starting movie");
        SurfaceHolder holder = getmSurfaceView().getHolder();
        Surface surface = holder.getSurface();

        // Don't leave the last frame of the previous video hanging on the screen.
        // Looks weird if the aspect ratio changes.
        clearSurface(surface);

        VideoPlayer player;
        try {
            Side servingSide = tryGettingServingSideFromXML(BENCHMARK_PREFIX + mTestSets[currentTestSet]);
            mHandler.initBenchmarkMatch(servingSide);
            player = new VideoPlayer(getCurrentFileManager().open(mTestSetClips[currentClip]), surface,
                    new SpeedControlCallback(), mHandler);
            Config mConfig = new Config(this);
            Table table = trySettingTableLocationFromXML(BENCHMARK_PREFIX + mTestSets[currentTestSet]);
            if (table != null) {
                mHandler.init(mConfig, player.getVideoWidth(), player.getVideoHeight(), table, VIEWING_ANGLE_HORIZONTAL);
                mHandler.startDetections();
            } else {
                Toast.makeText(this, "unable to initialize, please select the table again", Toast.LENGTH_LONG).show();
            }
        } catch (IOException ioe) {
            Log.e("Unable to play movie", ioe);
            surface.release();
        }
    }

    private void playCurrentClip() {
        SurfaceHolder holder = getmSurfaceView().getHolder();
        Surface surface = holder.getSurface();
        try {
            VideoPlayer player = new VideoPlayer(getCurrentFileManager().open(mTestSetClips[currentClip]), surface,
                    new SpeedControlCallback(), mHandler);
            mPlayTask = new VideoPlayer.PlayTask(player, this, false);
            mShowStopLabel = true;
            updateControls();
            mHandler.setClipId(mTestSetClips[currentClip]);
            mPlayTask.execute();
        } catch (IOException ex) {
            Log.e("Unable to play movie", ex);
            surface.release();
        }
    }
}
