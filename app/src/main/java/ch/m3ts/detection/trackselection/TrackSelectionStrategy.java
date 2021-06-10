package ch.m3ts.detection.trackselection;

import java.util.List;

import cz.fmo.data.Track;

public interface TrackSelectionStrategy {
    Track selectTrack(List<Track> tracks, int previousDirectionX, int previousDirectionY, int previousCenterX, int previousCenterY);
}