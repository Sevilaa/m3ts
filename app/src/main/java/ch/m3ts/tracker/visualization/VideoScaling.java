package ch.m3ts.tracker.visualization;

import android.graphics.Point;

/**
 * Helper class which scales coordinates and points to custom video resolutions (here referenced as
 * canvas).
 */
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
        if (this.canvasHeight == 0) {
            throw new NoDestinationResolutionSpecifiedException();
        }
        float relPercentage = (value) / ((float) this.videoHeight);
        return Math.round(relPercentage * this.canvasHeight);
    }

    public int scaleX(int value) {
        if (this.canvasWidth == 0) {
           throw new NoDestinationResolutionSpecifiedException();
        }
        float relPercentage = ((float) value) / ((float) this.videoWidth);
        return Math.round(relPercentage * this.canvasWidth);
    }

    public Point scalePoint(Point p) {
        return new Point(scaleX(p.x), scaleY(p.y));
    }

    static class NoDestinationResolutionSpecifiedException extends RuntimeException {
        private static final String MESSAGE = "Tried to scale using a destination resolution of which width and or height equals zero.";
        NoDestinationResolutionSpecifiedException() {
            super(MESSAGE);
        }
    }
}
