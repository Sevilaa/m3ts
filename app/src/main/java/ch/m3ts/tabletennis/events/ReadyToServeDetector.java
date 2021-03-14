package ch.m3ts.tabletennis.events;

import android.graphics.Point;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.helper.Side;

import static org.opencv.core.Core.inRange;

/**
 * Takes a frame in YUV format and checks if a player held his racket in the ready to serve area.
 **/
public class ReadyToServeDetector {
    private Table table;
    private int cameraWidth;
    private int cameraHeight;
    private ReadyToServeCallback callback;
    private Side server;
    private int gestureFrameCounter = 0;
    private static final double PERCENTAGE_THRESHOLD = 0.15;
    private static final int GESTURE_HOLD_TIME_IN_FRAMES = 15;
    private static final double GESTURE_AREA_PERCENTAGE_RELATIVE_TO_TABLE = 0.1;
    private static final double MAX_COLOR_CHANNEL_OFFSET = 10;

    public ReadyToServeDetector(Table table, Side server, int cameraWidth, int cameraHeight, ReadyToServeCallback callback) {
        this.table = table;
        this.server = server;
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.callback = callback;
    }

    /**
     * Checks every three frames if the player held his racket into the ready for serve area,
     * If the gesture was active for 15 frames (0.5s on a 30 FPS camera) it returns true and
     * triggers an event
     * @param  frameYUVBytes  frame in YUV format
     * @return true if gesture was active for 15 frames and false otherwise
     */
    public boolean isReadyToServe(byte[] frameYUVBytes) {
        boolean isReady = false;
        gestureFrameCounter++;
        if(gestureFrameCounter % 3 == 0 && OpenCVLoader.initDebug()) {
            if(isRacketInArea(frameYUVBytes)) {
                if(gestureFrameCounter >= GESTURE_HOLD_TIME_IN_FRAMES) {
                    this.callback.onGestureDetected();
                    isReady = true;
                }
            } else {
                gestureFrameCounter = 0;
            }
        }
        return isReady;
    }

    private boolean isRacketInArea(byte[] frameYUVBytes) {
        return getRedPercentage(frameYUVBytes) > PERCENTAGE_THRESHOLD;
    }

    private double getRedPercentage(byte [] frameYUVBytes) {
        Mat yuv = new Mat(getYUVMatHeight(), this.cameraWidth, CvType.CV_8UC1);
        yuv.put( 0, 0, frameYUVBytes);

        Mat bgr = convertYUVToBGRInAreaSize(yuv);

        Mat maskWithInvert = segmentRedColorViaInverting(bgr);
        Mat maskWithTwoThresh = segmentRedColor(bgr);
        Mat mask = new Mat();
        Core.bitwise_or(maskWithInvert, maskWithTwoThresh, mask);
        return Core.countNonZero(mask) / (double)mask.total();
    }

    private Mat convertYUVToBGRInAreaSize(Mat yuv) {
        Mat bgr = new Mat();
        Imgproc.cvtColor(yuv, bgr, Imgproc.COLOR_YUV2BGR_NV21, 3);
        bgr = bgr.submat(getGestureArea());
        return bgr;
    }

    /**
     * Calculates square around table corner of server
     * @return  rect with with sides in the size of 10% of the table
     */
    private Rect getGestureArea() {
        int width = (int) (this.table.getWidth() * GESTURE_AREA_PERCENTAGE_RELATIVE_TO_TABLE);

        Point position = this.table.getCornerDownLeft();
        int positionX = position.x;
        if(this.server == Side.RIGHT) {
            position = this.table.getCornerDownRight();
            positionX = position.x - width;
            if(positionX<0) positionX = 0;
        }
        return new Rect(positionX, position.y - width/2, width, width);
    }

    /**
     * Segments red color from image with two color ranges.
     * Needs more resources than segmentation with inversion.
     * @param  bgr  mat with bgr color space
     * @return      image where red pixels in original images are 255 and others 0
     */
    private Mat segmentRedColor(Mat bgr) {
        Mat mask1 = new Mat();
        Mat mask2 = new Mat();
        Mat normal = new Mat();
        Mat hsv = new Mat();
        Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV, 3);
        inRange(hsv, new Scalar(0,120,50), new Scalar(MAX_COLOR_CHANNEL_OFFSET,255,255), mask1);
        inRange(hsv, new Scalar(170,120,50), new Scalar(180,255,255), mask2);
        Core.bitwise_or(mask1, mask2, normal);
        return mask1;
    }


    /**
     * Segments red color from image by inverting first and then looking for cyan parts in image.
     * Needs less resources than segmentRedColor.
     * @param  bgr  mat with bgr color space
     * @return      image where red pixels in original images are 255 and others 0
     */
    private Mat segmentRedColorViaInverting(Mat bgr) {
        Mat bgrInverted = new Mat();
        Mat hsvInverted = new Mat();
        Mat maskInv = new Mat();
        Core.bitwise_not(bgr, bgrInverted);
        Imgproc.cvtColor(bgrInverted, hsvInverted, Imgproc.COLOR_BGR2HSV);
        inRange(hsvInverted, new Scalar(90 - MAX_COLOR_CHANNEL_OFFSET, 70, 50), new Scalar(90 + MAX_COLOR_CHANNEL_OFFSET, 255, 255), maskInv);
        return maskInv;
    }

    private int getYUVMatHeight() {
        return this.cameraHeight + this.cameraHeight/2;
    }

}
