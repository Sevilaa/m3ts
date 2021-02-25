package ch.m3ts.tracker.visualization;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import cz.fmo.R;
import cz.fmo.graphics.EGL;

@SuppressWarnings("squid:S110")
public abstract class MatchVisualizeActivity extends CameraPreviewActivity implements SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private SurfaceView mSurfaceTrack;
    private SurfaceView mSurfaceTable;
    private SurfaceView mSurfaceReplay;
    private TextView mBounceCountText;

    /**
     * In this method the inherited class must set the content view.
     *  i.E. setContentView(R.layout.activity_play_movie_surface);
     */
    public abstract void setCurrentContentView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCurrentContentView();
        mBounceCountText = findViewById(R.id.txtBounce);
        mSurfaceView = findViewById(R.id.playMovie_surface);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceTrack = findViewById(R.id.playMovie_surfaceTracks);
        mSurfaceTrack.setZOrderOnTop(true);
        mSurfaceTrack.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceTable = findViewById(R.id.playMovie_surfaceTable);
        mSurfaceTable.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceReplay = findViewById(R.id.playMovie_replaySurface);
        mSurfaceReplay.getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    public SurfaceView getmSurfaceView() {
        return mSurfaceView;
    }

    public SurfaceView getmSurfaceTrack() {
        return mSurfaceTrack;
    }

    public SurfaceView getmSurfaceTable() {
        return mSurfaceTable;
    }

    public SurfaceView getmSurfaceReplay() {
        return mSurfaceReplay;
    }

    public TextView getmBounceCountText() {
        return mBounceCountText;
    }
}
