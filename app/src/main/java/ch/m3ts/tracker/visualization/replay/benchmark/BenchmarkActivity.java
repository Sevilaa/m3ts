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

import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import ch.m3ts.tracker.visualization.replay.lib.SpeedControlCallback;
import ch.m3ts.tracker.visualization.replay.lib.VideoPlayer;
import ch.m3ts.util.Log;
import ch.m3ts.util.XMLLoader;
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
    private int[] nTotalJudgements;
    private int[] nCorrectJudgements;
    private boolean mShowStopLabel;
    private boolean doCancelBenchmark;
    private VideoPlayer.PlayTask mPlayTask;
    private BenchmarkHandler mHandler;
    private BenchmarkClipManager clipManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHandler = new BenchmarkHandler(this);
        String[] testSets = getResources().getStringArray(R.array.testSets);
        String[][] clips = new String[testSets.length][];
        this.nCorrectJudgements = new int[testSets.length];
        this.nTotalJudgements = new int[testSets.length];
        this.doCancelBenchmark = false;

        for (int i = 0; i < testSets.length; i++) {
            FileManager fm = new FileManager(this, testSets[i]);
            clips[i] = fm.listMP4();
            this.nTotalJudgements[i] = clips[i].length;
        }

        this.clipManager = new BenchmarkClipManager(clips, testSets);
        Button playStopButton = findViewById(R.id.benchmark_start_button);
        playStopButton.setOnClickListener(this);
        updateControls();
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
            if (!clipManager.advanceToNextClip()) {
                this.finishCurrentTestSet();
                if (!clipManager.advanceToNextTestSet()) {
                    finishBenchmark();
                    return;
                } else {
                    this.initCurrentTestSet();
                }
            }
            setWhoShouldScore();
            playCurrentClip();
        }
    }

    private void setWhoShouldScore() {
        Side sideToScore = clipManager.readWhichSideShouldScore();
        mHandler.setWhoShouldScore(sideToScore);
    }


    private void finishBenchmark() {
        this.onPause();
        updateControls();
        printStatistics();
        Toast.makeText(this, R.string.benchmark_finished_toast_text, Toast.LENGTH_LONG).show();
    }

    private void printStatistics() {
        String stats = BenchmarkClipManager.makeStatisticsString(nTotalJudgements, nCorrectJudgements, clipManager);
        Log.d(stats);
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

    private void finishCurrentTestSet() {
        this.nCorrectJudgements[clipManager.getCurrentTestSetId()] = mHandler.getCorrectJudgementCalls();
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
            Side servingSide = XMLLoader.loadServingSide(BENCHMARK_PREFIX + clipManager.getCurrentTestSet(), getAssets());
            mHandler.initBenchmarkMatch(servingSide);
            FileManager fileManager = new FileManager(getApplicationContext(), clipManager.getCurrentTestSet());
            player = new VideoPlayer(fileManager.open(clipManager.getCurrentClip()), surface,
                    new SpeedControlCallback(), mHandler);
            Config mConfig = new Config(this);
            Table table = XMLLoader.loadTable(BENCHMARK_PREFIX + clipManager.getCurrentTestSet(), getAssets());
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
            FileManager fileManager = new FileManager(getApplicationContext(), clipManager.getCurrentTestSet());
            VideoPlayer player = new VideoPlayer(fileManager.open(clipManager.getCurrentClip()), surface,
                    new SpeedControlCallback(), mHandler);
            mPlayTask = new VideoPlayer.PlayTask(player, this, false);
            mShowStopLabel = true;
            updateControls();
            mHandler.setClipId(clipManager.getCurrentClip());
            mPlayTask.execute();
        } catch (IOException ex) {
            Log.e("Unable to play movie", ex);
            surface.release();
        }
    }
}