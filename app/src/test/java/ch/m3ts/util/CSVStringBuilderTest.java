package ch.m3ts.util;

import org.junit.Test;

import java.util.List;
import java.util.Random;

import ch.m3ts.tabletennis.helper.Side;
import cz.fmo.Lib;
import cz.fmo.data.Track;
import cz.fmo.data.TrackSet;
import cz.fmo.util.Config;
import helper.DetectionGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSVStringBuilderTest {


    @Test
    public void testMakeCSVString() {
        assertNotNull(CSVStringBuilder.builder());
        assertEquals("", CSVStringBuilder.builder().toString());
        assertEquals("some;random;values;", CSVStringBuilder.builder()
                .add("some")
                .add("random")
                .add("values")
                .toString());

        assertEquals(";", CSVStringBuilder.builder().add("").toString());

        assertEquals("TOP;1;3;312;LEFT;RIGHT;BOTTOM;", CSVStringBuilder.builder()
                .add(Side.TOP)
                .add(1)
                .add(3)
                .add(312)
                .add(Side.LEFT)
                .add(Side.RIGHT)
                .add(Side.BOTTOM)
                .toString());
    }

    @Test
    public void makeCSVStringFromTracks() {
        Config mockConfig = mock(Config.class);
        TrackSet realTrackSet = TrackSet.getInstance();
        when(mockConfig.getFrameRate()).thenReturn(30f);
        when(mockConfig.getVelocityEstimationMode()).thenReturn(Config.VelocityEstimationMode.PX_FR);
        when(mockConfig.getObjectRadius()).thenReturn(10f);
        realTrackSet.setConfig(mockConfig);
        Lib.Detection[] detections = DetectionGenerator.makeFullDetections();
        realTrackSet.addDetections(detections, 1920, 1080, 0);
        assertEquals(createCSVStringFromDetections(detections, realTrackSet.getTracks()), CSVStringBuilder.builder()
                .add(realTrackSet.getTracks()).toString());
    }

    private String createCSVStringFromDetections(Lib.Detection[] detections, List<Track> tracks) {
        StringBuilder result = new StringBuilder();
        Random rd = new Random();
        for(int i = 0; i < detections.length; i++) {
            Side striker = rd.nextBoolean() ? Side.LEFT : Side.RIGHT;
            tracks.get(i).setStriker(striker);
            result.append(formatCSVString(new Object[]{i, detections[i].centerX, detections[i].centerY, detections[i].centerZ, detections[i].velocity, striker, detections[i].isBounce}));
        }
        return result.toString();
    }

    private String formatCSVString(Object[] data) {
        StringBuilder result = new StringBuilder();
        String fieldSeparator = "_";
        String cellSeparator = ";";
        for(int i = 0; i < data.length-1; i++) {
            result.append(data[i]).append(fieldSeparator);
        }
        result.append(data[data.length - 1]).append(cellSeparator);
        return result.toString();
    }
}