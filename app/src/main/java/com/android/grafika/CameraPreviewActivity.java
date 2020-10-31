package com.android.grafika;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import cz.fmo.R;

/**
 * Activity which uses the camera to preview on a SurfaceView
 */
public abstract class CameraPreviewActivity extends Activity implements SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private boolean mSurfaceHolderReady = false;

    /**
     * In this method the inherited class must set the content view.
     *  i.E. setContentView(R.layout.activity_play_movie_surface);
     */
    public abstract void setCurrentContentView();

    /**
     * In this method the inherited class initialize everything that is needed after
     * the surface has been created.
     */
    public abstract void init();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCurrentContentView();
        mSurfaceView = findViewById(R.id.playMovie_surface);
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // There's a short delay between the start of the activity and the initialization
        // of the SurfaceHolder that backs the SurfaceView.  We don't want to try to
        // send a video stream to the SurfaceView before it has initialized, so we disable
        // the "play" button until this callback fires.
        Log.d("surfaceCreated");
        mSurfaceHolderReady = true;
        init();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // ignore
        Log.d("surfaceChanged fmt=" + format + " size=" + width + "x" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // ignore
        Log.d("Surface destroyed");
    }

    public SurfaceView getmSurfaceView() {
        return mSurfaceView;
    }

    public boolean ismSurfaceHolderReady() {
        return mSurfaceHolderReady;
    }

    public void setmSurfaceHolderReady(boolean mSurfaceHolderReady) {
        this.mSurfaceHolderReady = mSurfaceHolderReady;
    }
}
