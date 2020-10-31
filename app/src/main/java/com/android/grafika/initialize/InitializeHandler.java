package com.android.grafika.initialize;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;

import com.android.grafika.CameraPreviewActivity;
import com.otaliastudios.zoom.ZoomLayout;

import java.lang.ref.WeakReference;

import cz.fmo.camera.CameraThread;

public class InitializeHandler extends android.os.Handler  implements CameraThread.Callback {
    private static final int CAMERA_ERROR = 2;
    final WeakReference<CameraPreviewActivity> mActivity;

    public InitializeHandler(@NonNull CameraPreviewActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void onCameraRender() {
        // draw table corners
        InitializeActivity activity = (InitializeActivity) mActivity.get();
        if (activity.ismSurfaceHolderReady()) {
            Canvas canvas = activity.getTableSurface().getHolder().lockCanvas();
            ZoomLayout zoomLayout = activity.getZoomLayout();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            Paint paint = new Paint();
            paint.setColor(Color.CYAN);
            paint.setStrokeWidth(15f);
            for (Point p : activity.getTableCorners()) {
                if(p != null) {
                    Point relP = makeRelPoint(p.x, p.y, zoomLayout.getZoom(), zoomLayout.getPanX(), zoomLayout.getPanY());
                    canvas.drawCircle(relP.x, relP.y, 20f, paint);
                }
            }
            activity.getTableSurface().getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private Point makeRelPoint(float absX, float absY, float zoom, float panX, float panY) {
        float relX = (absX - Math.abs(panX)) * zoom;
        float relY = (absY - Math.abs(panY)) * zoom;
        return new Point(Math.round(relX), Math.round(relY));
    }

    @Override
    public void onCameraFrame(byte[] dataYUV420SP) {
        // not needed, no processing of camera frame in this activity
    }

    @Override
    public void onCameraError() {
        if (hasMessages(CAMERA_ERROR)) return;
        sendMessage(obtainMessage(CAMERA_ERROR));
    }
}
