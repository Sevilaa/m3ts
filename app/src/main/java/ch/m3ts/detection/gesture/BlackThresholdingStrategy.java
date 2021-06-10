package ch.m3ts.detection.gesture;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.inRange;

public class BlackThresholdingStrategy implements GestureDetectionStrategy {
    private static final double PERCENTAGE_THRESHOLD_BLACK = 0.8;

    @Override
    public boolean isRacketInPicture(Mat bgr) {
        Mat mask = segmentBlackColor(bgr);
        double percentBlack = Core.countNonZero(mask) / (double) mask.total();
        return percentBlack > PERCENTAGE_THRESHOLD_BLACK;
    }

    /**
     * Segments black color from image with two color ranges.
     *
     * @param bgr mat with bgr color space
     * @return image where black pixels in original images are 255 and others 0
     */
    private Mat segmentBlackColor(Mat bgr) {
        Mat mask1 = new Mat();
        Mat hsv = new Mat();
        Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV, 3);
        inRange(hsv, new Scalar(0, 0, 0), new Scalar(255, 255, 70), mask1);
        return mask1;
    }
}
