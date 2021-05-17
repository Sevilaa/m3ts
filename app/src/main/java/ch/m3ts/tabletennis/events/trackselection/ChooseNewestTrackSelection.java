package ch.m3ts.tabletennis.events.trackselection;

import java.util.List;

import cz.fmo.data.Track;

public class ChooseNewestTrackSelection implements TrackSelectionStrategy {

    @Override
    public Track selectTrack(List<Track> tracks, int previousDirectionX, int previousDirectionY, int previousCenterX, int previousCenterY) {
        Track selectedTrack = null;
        for (int i = tracks.size() - 1; i >= 0; i--) {
            Track t = tracks.get(i);
            if (t.hasCrossedTable()) {
                selectedTrack = t;
                break;
            }
        }
        return selectedTrack;
    }
}