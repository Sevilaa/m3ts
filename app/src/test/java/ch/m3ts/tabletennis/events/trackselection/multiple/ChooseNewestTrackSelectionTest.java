package ch.m3ts.tabletennis.events.trackselection.multiple;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import cz.fmo.data.Track;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class ChooseNewestTrackSelectionTest {
    private ChooseNewestTrackSelection trackSelection;
    private List<Track> mockedTracks;

    @Before
    public void init() {
        trackSelection = new ChooseNewestTrackSelection();
        mockedTracks = new ArrayList<>();
        Track track = Mockito.mock(Track.class);
        when(track.hasCrossedTable()).thenReturn(true);
        mockedTracks.add(track);
        Track trackSecond = Mockito.mock(Track.class);
        when(trackSecond.hasCrossedTable()).thenReturn(true);
        mockedTracks.add(trackSecond);
    }

    @Test
    public void selectTrack() {
        Track selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 0, 0);
        assertEquals(mockedTracks.get(1), selectedTrack);
        assertNotNull(selectedTrack);

        // now add an invalid track to the list (newest track invalid)
        Track trackInvalid = Mockito.mock(Track.class);
        when(trackInvalid.hasCrossedTable()).thenReturn(false);
        mockedTracks.add(trackInvalid);
        selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 0, 0);
        assertEquals(mockedTracks.get(1), selectedTrack);

        // now list with only invalid tracks
        mockedTracks.clear();
        mockedTracks.add(trackInvalid);
        mockedTracks.add(trackInvalid);
        mockedTracks.add(trackInvalid);
        selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 0, 0);
        assertNull(selectedTrack);

        // empty list
        mockedTracks.clear();
        selectedTrack = trackSelection.selectTrack(mockedTracks, 0, 0, 0, 0);
        assertNull(selectedTrack);
    }
}