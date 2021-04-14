package ch.m3ts.event.data.eventdetector;

import ch.m3ts.tabletennis.events.EventDetectionListener;
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
