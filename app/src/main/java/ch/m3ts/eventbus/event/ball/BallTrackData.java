package ch.m3ts.eventbus.event.ball;

import ch.m3ts.detection.EventDetectionListener;
import cz.fmo.data.Track;

public class BallTrackData implements EventDetectorEventData {
    private final Track track;

    public BallTrackData(Track track) {
        this.track = track;
    }

    @Override
    public void call(EventDetectionListener eventDetectionListener) {
        eventDetectionListener.onStrikeFound(track);
    }
}
