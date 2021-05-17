package ch.m3ts.tabletennis.events.trackselection.multiple;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import ch.m3ts.tabletennis.events.trackselection.MinimumDistanceTrackSelection;
import cz.fmo.Lib;
import cz.fmo.data.Track;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class MinimumDistanceTrackSelectionTest {
    private MinimumDistanceTrackSelection trackSelection;
    private List<Track> mockedTracks;

    @Before
    public void init() {
        trackSelection = new MinimumDistanceTrackSelection();
        mockedTracks = new ArrayList<>();
        Lib.Detection dRight = new Lib.Detection();
        dRight.centerX = 1000;
        Lib.Detection dLeft = new Lib.Detection();
        dLeft.centerX = 100;

        Track track = Mockito.mock(Track.class);
        when(track.hasCrossedTable()).thenReturn(true);
        when(track.getLatest()).thenReturn(dRight);
        mockedTracks.add(track);
        Track trackSecond = Mockito.mock(Track.class);
        when(trackSecond.hasCrossedTable()).thenReturn(true);
        when(trackSecond.getLatest()).thenReturn(dLeft);
        mockedTracks.add(trackSecond);
    }


    @Test
    public void selectTrack() {
        Track selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 0, 0);
        assertEquals(mockedTracks.get(1), selectedTrack);
        selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 700, 0);
        assertEquals(mockedTracks.get(0), selectedTrack);

        // should return null even when there was same direction but that track hasn't crossed the table
        when(mockedTracks.get(0).hasCrossedTable()).thenReturn(false);
        when(mockedTracks.get(1).hasCrossedTable()).thenReturn(false);
        selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 0, 0);
        assertNull(selectedTrack);

        // empty list
        mockedTracks.clear();
        selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 0, 0);
        assertNull(selectedTrack);
        selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 700, 0);
        assertNull(selectedTrack);
    }
}