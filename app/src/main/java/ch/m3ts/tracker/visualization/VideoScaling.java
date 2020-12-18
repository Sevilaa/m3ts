package ch.m3ts.tracker.visualization;

import android.graphics.Point;

public class VideoScaling {
    private int videoWidth;
    private int videoHeight;
    private int canvasWidth;
    private int canvasHeight;

    public VideoScaling(int videoWidth, int videoHeight) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public void setCanvasWidth(int canvasWidth) {
        this.canvasWidth = canvasWidth;
    }

    public void setCanvasHeight(int canvasHeight) {
        this.canvasHeight = canvasHeight;
    }

    public int scaleY(float value) {
        float relPercentage = (value) / ((float) this.videoHeight);
        return Math.round(relPercentage * this.canvasHeight);
    }

    public int scaleX(int value) {
        float relPercentage = ((float) value) / ((float) this.videoWidth);
        return Math.round(relPercentage * this.canvasWidth);
    }

    public Point scalePoint(Point p) {
        return new Point(scaleX(p.x), scaleY(p.y));
    }
}
