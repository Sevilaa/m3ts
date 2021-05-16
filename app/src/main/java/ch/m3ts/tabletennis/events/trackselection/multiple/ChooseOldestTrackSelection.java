package ch.m3ts.tabletennis.events.trackselection.multiple;

import java.util.List;

import ch.m3ts.tabletennis.events.trackselection.TrackSelectionStrategy;
import cz.fmo.data.Track;

public class ChooseOldestTrackSelection implements TrackSelectionStrategy {

    @Override
    public Track selectTrack(List<Track> tracks, int previousDirectionX, int previousDirectionY, int previousCenterX, int previousCenterY) {
        Track selectedTrack = null;
        for (Track t : tracks) {
            if (t.hasCrossedTable()) {
                selectedTrack = t;
                break;
            }
        }
        return selectedTrack;
    }
}
