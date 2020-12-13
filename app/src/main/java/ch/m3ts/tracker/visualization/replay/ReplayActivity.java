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

package ch.m3ts.tracker.visualization.replay;

import android.opengl.GLES20;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import ch.m3ts.Log;
import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tracker.visualization.MatchVisualizeActivity;
import cz.fmo.R;
import cz.fmo.graphics.EGL;
import cz.fmo.util.Config;
import cz.fmo.util.FileManager;

/**
 * Play a movie from a file on disk.  Output goes to a SurfaceView.
 * <p>
 * This is very similar to PlayMovieActivity, but the output goes to a SurfaceView instead of
 * a TextureView.  There are some important differences:
 * <ul>
 * <li> TextureViews behave like normal views.  SurfaceViews don't.  A SurfaceView has
 * a transparent "hole" in the UI through which an independent Surface layer can
 * be seen.  This Surface is sent directly to the system graphics compositor.
 * <li> Because the video is being composited with the UI by the system compositor,
 * rather than the application, it can often be done more efficiently (e.g. using
 * a hardware composer "overlay").  This can lead to significant battery savings
 * when playing a long movie.
 * <li> On the other hand, the TextureView contents can be freely scaled and rotated
 * with a simple matrix.  The SurfaceView output is limited to scaling, and it's
 * more awkward to do.
 * <li> DRM-protected content can't be touched by the app (or even the system compositor).
 * We have to point the MediaCodec decoder at a Surface that is composited by a
 * hardware composer overlay.  The only way to do the app side of this is with
 * SurfaceView.
 * </ul>
 * <p>
 * The MediaCodec decoder requests buffers from the Surface, passing the video dimensions
 * in as arguments.  The Surface provides buffers with a matching size, which means
 * the video data will completely cover the Surface.  As a result, there's no need to
 * use SurfaceHolder#setFixedSize() to set the dimensions.  The hardware scaler will scale
 * the video to match the view size, so if we want to preserve the correct aspect ratio
 * we need to adjust the View layout.  We can use our custom AspectFrameLayout for this.
 * <p>)
 * The actual playback of the video -- sending frames to a Surface -- is the same for
 * TextureView and SurfaceView.
 */
public class ReplayActivity extends MatchVisualizeActivity implements OnItemSelectedListener, VideoPlayer.PlayerFeedback {
    private final FileManager mFileMan = new FileManager(this);
    private String[] mMovieFiles;
    private int mSelectedMovie;
    private boolean mShowStopLabel;
    private VideoPlayer.PlayTask mPlayTask;
    private ReplayHandler mHandler;
    private String currentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Populate file-selection spinner.
        Spinner spinner = findViewById(R.id.playMovieFile_spinner);
        this.mHandler = new ReplayHandler(this);
        // Need to create one of these fancy ArrayAdapter thingies, and specify the generic layout
        // for the widget itself.
        mMovieFiles = mFileMan.listMP4();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mMovieFiles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        updateControls();
    }

    /**
     * onClick handler for "play"/"stop" button.
     */
    public void clickPlayStop(@SuppressWarnings("UnusedParameters") View unused) {
        if (mShowStopLabel) {
            Log.d("stopping movie");
            stopPlayback();
            // Don't update the controls here -- let the task thread do it after the movie has
            // actually stopped.
            //mShowStopLabel = false;
            //updateControls();
        } else {
            if (mPlayTask != null) {
                Log.w("movie already playing");
                return;
            }

            Log.d("starting movie");
            SpeedControlCallback callback = new SpeedControlCallback();
            SurfaceHolder holder = getmSurfaceView().getHolder();
            SurfaceHolder holderTracks = getmSurfaceTrack().getHolder();
            Surface surface = holder.getSurface();

            // Don't leave the last frame of the previous video hanging on the screen.
            // Looks weird if the aspect ratio changes.
            clearSurface(surface);
            mHandler.clearCanvas(holderTracks);

            VideoPlayer player;
            try {
                Side servingSide = tryGettingServingSideFromXML(mMovieFiles[mSelectedMovie]);
                mHandler.initMatch(servingSide);
                currentMovie = mMovieFiles[mSelectedMovie];
                player = new VideoPlayer(mFileMan.open(mMovieFiles[mSelectedMovie]), surface,
                        callback, mHandler);
                Config mConfig = new Config(this);
                mHandler.init(mConfig, player.getVideoWidth(), player.getVideoHeight());
                trySettingTableLocationFromXML(mMovieFiles[mSelectedMovie]);
                mHandler.drawTable();
                mHandler.startDetections();
            } catch (IOException ioe) {
                Log.e("Unable to play movie", ioe);
                surface.release();
                return;
            }

            mPlayTask = new VideoPlayer.PlayTask(player, this, true);
            mShowStopLabel = true;
            updateControls();
            mPlayTask.execute();
        }
    }

    @Override
    public void init() {
        updateControls();
    }

    @Override
    public void setCurrentContentView() {
        setContentView(R.layout.activity_play_movie_surface);
    }

    /**
     * Called when the movie Spinner gets touched.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Spinner spinner = (Spinner) parent;
        mSelectedMovie = spinner.getSelectedItemPosition();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Can ignore this event
    }

    @Override   // MoviePlayer.PlayerFeedback
    public void playbackStopped() {
        Log.d("playback stopped");
        mShowStopLabel = false;
        mPlayTask = null;
        updateControls();
    }

    @Override
    protected void onResume() {
        Log.d("PlayMovieSurfaceActivity onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("PlayMovieSurfaceActivity onPause");
        super.onPause();
        // We're not keeping track of the state in static fields, so we need to shut the
        // playback down.  Ideally we'd preserve the state so that the player would continue
        // after a device rotation.
        //
        // We want to be sure that the player won't continue to send frames after we pause,
        // because we're tearing the view down.  So we wait for it to stop here.
        if (mPlayTask != null) {
            stopPlayback();
            mPlayTask.waitForStop();
        }
        //here
        mHandler.stopDetections();
    }

    /**
     * Requests stoppage if a movie is currently playing.
     */
    private void stopPlayback() {
        if (mPlayTask != null) {
            mPlayTask.requestStop();
        }
    }

    /**
     * Updates the on-screen controls to reflect the current state of the app.
     */
    private void updateControls() {
        Button play = findViewById(R.id.play_stop_button);
        if (mShowStopLabel) {
            play.setText(R.string.stop_button_text);
        } else {
            play.setText(R.string.play_button_text);
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
     * @param videoFileName - Full name of video file in phones Camera dir. Example: "bounce_back_1.mp4"
     */
    private void trySettingTableLocationFromXML(String videoFileName) {
        String fileNameWithoutExtension = videoFileName.split("\\.")[0];
        try (InputStream is = getAssets().open(fileNameWithoutExtension+".xml")) {
            Properties properties = new Properties();
            properties.loadFromXML(is);
            mHandler.setTable(Table.makeTableFromProperties(properties));
            Log.d("found new table!");
        } catch (IOException ex) {
            Log.e(ex.getMessage(), ex);
        }
    }

    /**
     * Tries to load the serving side from an xml file from assets.
     * @param videoFileName - Full name of video file in phones Camera dir. Example: "bounce_back_1.mp4"
     */
    private Side tryGettingServingSideFromXML(String videoFileName) {
        Side servingSide = Side.LEFT;
        String fileNameWithoutExtension = videoFileName.split("\\.")[0];
        try (InputStream is = getAssets().open(fileNameWithoutExtension+".xml")) {
            Properties properties = new Properties();
            properties.loadFromXML(is);
            if (properties.containsKey("servingSide") && properties.getProperty("servingSide").equals("RIGHT"))
                servingSide = Side.RIGHT;
        } catch (IOException ex) {
            Log.e(ex.getMessage(), ex);
        }
        return servingSide;
    }
}
