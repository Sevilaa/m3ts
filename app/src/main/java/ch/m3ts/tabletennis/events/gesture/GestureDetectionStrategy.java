package ch.m3ts.tabletennis.events.gesture;

import org.opencv.core.Mat;

public interface GestureDetectionStrategy {
    boolean isRacketInPicture(Mat bgr);
}
