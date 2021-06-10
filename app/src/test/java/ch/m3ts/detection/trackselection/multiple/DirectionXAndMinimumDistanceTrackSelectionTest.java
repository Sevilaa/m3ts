package ch.m3ts.detection.trackselection.multiple;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import ch.m3ts.detection.trackselection.DirectionXAndMinimumDistanceTrackSelection;
import cz.fmo.Lib;
import cz.fmo.data.Track;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class DirectionXAndMinimumDistanceTrackSelectionTest {
    private DirectionXAndMinimumDistanceTrackSelection trackSelection;
    private List<Track> mockedTracks;

    @Before
    public void init() {
        trackSelection = new DirectionXAndMinimumDistanceTrackSelection();
        mockedTracks = new ArrayList<>();
        Lib.Detection dRight = new Lib.Detection();
        dRight.directionX = -1;
        dRight.centerX = 1000;
        Lib.Detection dLeft = new Lib.Detection();
        dLeft.directionX = 1;
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
        Track selectedTrack = trackSelection.selectTrack(mockedTracks, 1, 0, 900, 0);
        assertEquals(mockedTracks.get(1), selectedTrack);
        selectedTrack = trackSelection.selectTrack(mockedTracks, -1, 0, 100, 0);
        assertEquals(mockedTracks.get(0), selectedTrack);

        mockedTracks.get(1).getLatest().directionX = -1;
        selectedTrack = trackSelection.selectTrack(mockedTracks, 1, 0, 900, 0);
        assertEquals(mockedTracks.get(0), selectedTrack);

        // should return null even when no track has crossed the table
        when(mockedTracks.get(0).hasCrossedTable()).thenReturn(false);
        when(mockedTracks.get(1).hasCrossedTable()).thenReturn(false);
        selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 0, 0);
        assertNull(selectedTrack);

        // empty list
        mockedTracks.clear();
        selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 0, 0);
        assertNull(selectedTrack);
    }
}