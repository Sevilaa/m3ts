package ch.m3ts.detection.gesture;

import org.opencv.core.Mat;

public interface GestureDetectionStrategy {
    boolean isRacketInPicture(Mat bgr);
}
