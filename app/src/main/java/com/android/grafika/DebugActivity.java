package com.android.grafika;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import cz.fmo.R;

public abstract class DebugActivity extends CameraPreviewActivity implements SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private SurfaceView mSurfaceTrack;
    private SurfaceView mSurfaceTable;
    private TextView mShotSideText;
    private TextView mBounceCountText;
    private TextView mScoreLeftText;
    private TextView mScoreRightText;

    /**
     * In this method the inherited class must set the content view.
     *  i.E. setContentView(R.layout.activity_play_movie_surface);
     */
    public abstract void setCurrentContentView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCurrentContentView();
        mShotSideText = findViewById(R.id.txtSide);
        mBounceCountText = findViewById(R.id.txtBounce);
        mScoreLeftText = findViewById(R.id.txtPlayMovieScoreLeft);
        mScoreRightText = findViewById(R.id.txtPlayMovieScoreRight);
        mSurfaceView = findViewById(R.id.playMovie_surface);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceTrack = findViewById(R.id.playMovie_surfaceTracks);
        mSurfaceTrack.setZOrderOnTop(true);
        mSurfaceTrack.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceTable = findViewById(R.id.playMovie_surfaceTable);
        mSurfaceTable.getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    public SurfaceView getmSurfaceView() {
        return mSurfaceView;
    }

    public SurfaceView getmSurfaceTrack() {
        return mSurfaceTrack;
    }

    public SurfaceView getmSurfaceTable() {
        return mSurfaceTable;
    }

    public TextView getmShotSideText() {
        return mShotSideText;
    }

    public TextView getmBounceCountText() {
        return mBounceCountText;
    }

    public TextView getmScoreLeftText() {
        return mScoreLeftText;
    }

    public TextView getmScoreRightText() {
        return mScoreRightText;
    }
}
