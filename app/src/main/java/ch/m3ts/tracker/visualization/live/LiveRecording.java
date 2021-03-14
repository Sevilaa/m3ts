package ch.m3ts.tracker.visualization.live;

import android.content.Context;

import java.io.File;

import cz.fmo.camera.CameraThread;
import cz.fmo.camera.RecordingCameraTarget;
import cz.fmo.recording.CyclicBuffer;
import cz.fmo.recording.EncodeThread;
import cz.fmo.recording.ManualRecordingTask;
import cz.fmo.recording.SaveThread;
import cz.fmo.util.FileManager;

public class LiveRecording implements SaveThread.Callback {
    private static LiveRecording instance;
    private static final float BUFFER_SECONDS = 8;
    private static final String FILENAME = "recording_%s.mp4";
    private FileManager mFileMan;
    private EncodeThread mEncode;
    private SaveThread mSaveMovie;
    private SaveThread.Task mSaveTask;
    private RecordingCameraTarget mEncodeTarget;
    private boolean isRecording = false;

    private LiveRecording() {}

    public static LiveRecording getInstance(Context ctx, CameraThread mCamera) {
        if (instance == null) {
            instance = new LiveRecording();
        }
        instance.setup(ctx, mCamera);
        return instance;
    }

    @Override
    public void saveCompleted(File file, boolean success) {
        stopSaving();

        if (success) {
            mFileMan.newMedia(file);
        }

        mSaveTask = null;
    }

    public void tearDown() {
        stopSaving();

        if (mSaveMovie != null) {
            mSaveMovie.getHandler().sendKill();
            try {
                mSaveMovie.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                ch.m3ts.Log.e("Interrupted when closing SaveThread", ie);
            }
            mSaveMovie = null;
        }

        if (mEncode != null) {
            mEncode.getHandler().sendKill();
            try {
                mEncode.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                ch.m3ts.Log.e("Interrupted when closing EncodeThread", ie);
            }
            mEncode = null;
        }
    }

    public EncodeThread getmEncode() {
        return mEncode;
    }

    public void startRecording() {
        isRecording = true;
        setEncodingEnabled(true);
        File outFile = mFileMan.open(String.format(FILENAME, System.currentTimeMillis()/1000));
        mSaveTask = new ManualRecordingTask(outFile, mSaveMovie);
    }

    public boolean isRecording() {
        return isRecording;
    }

    private void setup(Context ctx, CameraThread mCamera) {
        this.mFileMan = new FileManager(ctx);
        // if recording is enabled...
        // make a suitably-sized cyclic buffer
        CyclicBuffer buffer = new CyclicBuffer(mCamera.getBitRate(), mCamera.getFrameRate(),
                BUFFER_SECONDS);

        // create dedicated encoding and video saving threads
        mEncode = new EncodeThread(mCamera.getMediaFormat(), buffer);
        mSaveMovie = new SaveThread(buffer, this);

        // add encoder as camera target
        mEncodeTarget = new RecordingCameraTarget(mEncode.getInputSurface(),
                mCamera.getWidth(), mCamera.getHeight());
        mCamera.addTarget(mEncodeTarget);
        setEncodingEnabled(false);

        mEncode.start();
        mSaveMovie.start();
    }

    /**
     * Enabled or disables encoding, along with any possibility of recording. In idle state,
     * encoding is disabled to save battery; while recording (both manually or automatically),
     * encoding is enabled.
     */
    private void setEncodingEnabled(boolean enabled) {
        stopSaving();

        if (mEncode != null) {
            mEncode.clearBuffer();
        }

        if (mEncodeTarget != null) {
            mEncodeTarget.setEnabled(enabled);
        }
    }

    /**
     * Ceases any saving operation, scheduled or in progress.
     */
    private void stopSaving() {
        if (mSaveTask != null) {
            mSaveTask.terminate();
            mSaveTask = null;
        }
    }
}
