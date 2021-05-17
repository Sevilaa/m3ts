package ch.m3ts.tabletennis.events.gesture;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

public class BackgroundModelingStrategy implements GestureDetectionStrategy {
    private static final double PERCENTAGE_THRESHOLD = 0.6;
    private Mat fgMask;
    private final BackgroundSubtractor backSub;

    public BackgroundModelingStrategy() {
        OpenCVLoader.initDebug();   // pls no delete, need to init here because of unit testing...
        backSub = Video.createBackgroundSubtractorKNN();
    }

    @Override
    public boolean isRacketInPicture(Mat bgr) {
        if (fgMask == null) {
            fgMask = bgr;
            return false;
        } else {
            backSub.apply(bgr, fgMask);
            double percentNotInBackground = Core.countNonZero(fgMask) / (double) fgMask.total();
            return percentNotInBackground > PERCENTAGE_THRESHOLD && percentNotInBackground != 1.0;
        }
    }
}
