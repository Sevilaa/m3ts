package ch.m3ts.eventbus.event.ball;

import ch.m3ts.detection.EventDetectionListener;
import ch.m3ts.util.Side;

public class BallBounceAudioData implements EventDetectorEventData {
    private final Side tableSide;

    public BallBounceAudioData(Side tableSide) {
        this.tableSide = tableSide;
    }

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onAudioBounce(tableSide);
    }
}
