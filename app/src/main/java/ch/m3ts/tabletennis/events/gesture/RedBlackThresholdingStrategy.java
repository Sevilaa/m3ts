package ch.m3ts.tabletennis.events.gesture;

import org.opencv.core.Mat;

public class RedBlackThresholdingStrategy implements GestureDetectionStrategy {
    private BlackThresholdingStrategy blackThresholdingStrategy;
    private RedThresholdingStrategy redThresholdingStrategy;

    public RedBlackThresholdingStrategy() {
        this.blackThresholdingStrategy = new BlackThresholdingStrategy();
        this.redThresholdingStrategy = new RedThresholdingStrategy();
    }

    @Override
    public boolean isRacketInPicture(Mat bgr) {
        return redThresholdingStrategy.isRacketInPicture(bgr) || blackThresholdingStrategy.isRacketInPicture(bgr);
    }
}
