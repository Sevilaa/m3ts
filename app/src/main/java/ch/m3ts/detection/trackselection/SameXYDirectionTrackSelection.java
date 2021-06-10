package ch.m3ts.detection.trackselection;

import java.util.List;

import cz.fmo.Lib;
import cz.fmo.data.Track;

public class SameXYDirectionTrackSelection implements TrackSelectionStrategy {
    @Override
    public Track selectTrack(List<Track> tracks, int previousDirectionX, int previousDirectionY, int previousCenterX, int previousCenterY) {
        Track selectedTrack = null;
        for (int i = tracks.size() - 1; i >= 0; i--) {
            Track t = tracks.get(i);
            Lib.Detection d = t.getLatest();
            if (d.directionX == previousDirectionX && d.directionY == previousDirectionY && t.hasCrossedTable()) {
                selectedTrack = t;
            }
        }
        return selectedTrack;
    }
}
