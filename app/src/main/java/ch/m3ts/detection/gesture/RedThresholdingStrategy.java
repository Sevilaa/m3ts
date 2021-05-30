package ch.m3ts.detection.gesture;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.inRange;

public class RedThresholdingStrategy implements GestureDetectionStrategy {
    private static final double PERCENTAGE_THRESHOLD = 0.5;
    private static final double MAX_COLOR_CHANNEL_OFFSET = 10;

    @Override
    public boolean isRacketInPicture(Mat bgr) {
        Mat maskWithInvert = segmentRedColorViaInverting(bgr);
        Mat maskWithTwoThresh = segmentRedColor(bgr);
        Mat mask = new Mat();
        Core.bitwise_or(maskWithInvert, maskWithTwoThresh, mask);
        double percentRed = Core.countNonZero(mask) / (double) mask.total();
        return percentRed > PERCENTAGE_THRESHOLD;
    }

    /**
     * Segments red color from image with two color ranges.
     * Needs more resources than segmentation with inversion.
     *
     * @param bgr mat with bgr color space
     * @return image where red pixels in original images are 255 and others 0
     */
    private Mat segmentRedColor(Mat bgr) {
        Mat mask1 = new Mat();
        Mat mask2 = new Mat();
        Mat normal = new Mat();
        Mat hsv = new Mat();
        Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV, 3);
        inRange(hsv, new Scalar(0, 120, 50), new Scalar(MAX_COLOR_CHANNEL_OFFSET, 255, 255), mask1);
        inRange(hsv, new Scalar(170, 120, 50), new Scalar(180, 255, 255), mask2);
        Core.bitwise_or(mask1, mask2, normal);
        return mask1;
    }

    /**
     * Segments red color from image by inverting first and then looking for cyan parts in image.
     * Needs less resources than segmentRedColor.
     *
     * @param bgr mat with bgr color space
     * @return image where red pixels in original images are 255 and others 0
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
}
