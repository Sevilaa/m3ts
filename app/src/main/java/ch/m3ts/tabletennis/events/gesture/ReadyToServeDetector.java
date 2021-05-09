package ch.m3ts.tabletennis.events.gesture;

import android.graphics.Point;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.helper.Side;

/**
 * Takes a frame in YUV format and checks if a player held his racket in the ready to serve area.
 **/
public class ReadyToServeDetector {
    private static final int GESTURE_HOLD_TIME_IN_FRAMES = 15;
    private static final double GESTURE_AREA_PERCENTAGE_RELATIVE_TO_TABLE = 0.05;
    private final Table table;
    private final ReadyToServeCallback callback;
    private final Side server;
    private final GestureDetectionStrategy gestureDetectionStrategy;
    private int gestureFrameCounter = 0;

    public ReadyToServeDetector(Table table, Side server, ReadyToServeCallback callback, boolean useRedSideOnly) {
        this.table = table;
        this.server = server;
        this.callback = callback;
        if (useRedSideOnly) {
            this.gestureDetectionStrategy = new RedThresholdingStrategy();
        } else {
            this.gestureDetectionStrategy = new BackgroundModelingStrategy();
        }
    }

    /**
     * Checks every three frames if the player held his racket into the ready for serve area,
     * If the gesture was active for 15 frames (0.5s on a 30 FPS camera) it returns true and
     * triggers an event
     *
     * @param bgrMat frame as a BGR OpenCV Mat
     * @return true if gesture was active for 15 frames and false otherwise
     */
    public boolean isReadyToServe(Mat bgrMat) {
        boolean isReady = false;
        if (OpenCVLoader.initDebug()) {
            gestureFrameCounter++;
            if (gestureFrameCounter % 3 == 0) {
                if (isRacketInArea(bgrMat)) {
                    if (gestureFrameCounter >= GESTURE_HOLD_TIME_IN_FRAMES) {
                        // TODO: change this to use eventbus
                        this.callback.onGestureDetected();
                        isReady = true;
                    }
                } else {
                    gestureFrameCounter = 0;
                }
            }
        }
        return isReady;
    }

    private boolean isRacketInArea(Mat bgrMat) {
        Mat resizedMat = resizeMatToAreaSize(bgrMat);
        return gestureDetectionStrategy.isRacketInPicture(resizedMat);
    }

    private Mat resizeMatToAreaSize(Mat bgrMat) {
        return bgrMat.submat(getGestureArea());
    }

    /**
     * Calculates square around table corner of server
     *
     * @return rect with with sides in the size of GESTURE_AREA_PERCENTAGE_RELATIVE_TO_TABLE of the table width
     */
    private Rect getGestureArea() {
        int width = (int) (this.table.getWidth() * GESTURE_AREA_PERCENTAGE_RELATIVE_TO_TABLE);
        Point position = this.table.getCornerDownLeft();
        int positionX = position.x;
        if (this.server == Side.RIGHT) {
            position = this.table.getCornerDownRight();
            positionX = position.x - width;
            if (positionX < 0) positionX = 0;
        }
        return new Rect(positionX, position.y - width / 2, width, width);
    }
}
