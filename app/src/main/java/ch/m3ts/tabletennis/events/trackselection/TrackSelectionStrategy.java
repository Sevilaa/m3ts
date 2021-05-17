package ch.m3ts.tabletennis.events.trackselection;

import java.util.List;

import cz.fmo.data.Track;

public interface TrackSelectionStrategy {
    Track selectTrack(List<Track> tracks, int previousDirectionX, int previousDirectionY, int previousCenterX, int previousCenterY);
}