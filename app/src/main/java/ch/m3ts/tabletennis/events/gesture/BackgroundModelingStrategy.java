package ch.m3ts.tabletennis.events.gesture;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

public class BackgroundModelingStrategy implements GestureDetectionStrategy {
    private static final double PERCENTAGE_THRESHOLD = 0.9;
    private final Mat fgMask;
    private final BackgroundSubtractor backSub;

    public BackgroundModelingStrategy() {
        fgMask = new Mat();
        backSub = Video.createBackgroundSubtractorKNN();
    }

    @Override
    public boolean isRacketInPicture(Mat bgr) {
        backSub.apply(bgr, fgMask);
        double percentNotInBackground = Core.countNonZero(fgMask) / (double) fgMask.total();
        return percentNotInBackground > PERCENTAGE_THRESHOLD;
    }
}
