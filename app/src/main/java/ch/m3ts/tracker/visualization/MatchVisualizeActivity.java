package ch.m3ts.tracker.visualization;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import cz.fmo.R;

public abstract class MatchVisualizeActivity extends CameraPreviewActivity implements SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private SurfaceView mSurfaceTrack;
    private SurfaceView mSurfaceTable;
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

    public TextView getmBounceCountText() {
        return mBounceCountText;
    }
}
