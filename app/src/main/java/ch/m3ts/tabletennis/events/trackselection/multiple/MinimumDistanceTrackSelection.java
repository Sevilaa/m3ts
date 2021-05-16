package ch.m3ts.tabletennis.events.trackselection.multiple;

import java.util.List;

import ch.m3ts.tabletennis.events.trackselection.TrackSelectionStrategy;
import cz.fmo.Lib;
import cz.fmo.data.Track;

public class MinimumDistanceTrackSelection implements TrackSelectionStrategy {

    @Override
    public Track selectTrack(List<Track> tracks, int previousDirectionX, int previousDirectionY, int previousCenterX, int previousCenterY) {
        Track selectedTrack = null;
        double distance = Double.MAX_VALUE;
        for (Track t : tracks) {
            Lib.Detection d = t.getLatest();
            double a = Math.abs(d.centerX - previousCenterX);
            double b = Math.abs(d.centerY - previousCenterY);
            double distanceToLast = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
            if (distanceToLast < distance && t.hasCrossedTable()) {
                selectedTrack = t;
                distance = distanceToLast;
            }
        }
        return selectedTrack;
    }
}
