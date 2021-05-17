package ch.m3ts.display.stats.processing;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.m3ts.display.stats.DetectionData;
import ch.m3ts.display.stats.TrackData;
import ch.m3ts.tabletennis.helper.DirectionX;
import ch.m3ts.tabletennis.helper.Side;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StatsProcessingTest {
    List<TrackData> trackDataList;
    DetectionData[] detectionDataArr;
    List<DetectionData>[] detectionDataListArr;

    @Before
    public void initTestData() {
        trackDataList = new LinkedList<>();
        int nTracks = 5;
        detectionDataArr = new DetectionData[]{
                new DetectionData(10, 10, 1, 100f, false, DirectionX.RIGHT),
                new DetectionData(20, 10, 1, 200f, false, DirectionX.RIGHT),
                new DetectionData(30, 10, 1, 300f, false, DirectionX.RIGHT),
                new DetectionData(25, 10, 1, 100f, false, DirectionX.LEFT),
                new DetectionData(15, 10, 1, 200f, false, DirectionX.LEFT)
        };

        detectionDataListArr = new LinkedList[nTracks];
        for (int i = 0; i < nTracks; i++) {
            detectionDataListArr[i] = new LinkedList<>();
            detectionDataListArr[i].add(detectionDataArr[i]);
        }

        trackDataList.add(new TrackData(detectionDataListArr[0], 100f, Side.LEFT));
        trackDataList.add(new TrackData(detectionDataListArr[1], 200f, Side.LEFT));
        trackDataList.add(new TrackData(detectionDataListArr[2], 300f, Side.LEFT));
        trackDataList.add(new TrackData(detectionDataListArr[3], 100f, Side.RIGHT));
        trackDataList.add(new TrackData(detectionDataListArr[4], 200f, Side.RIGHT));
    }

    @Test
    public void putTogetherTracksOfSameStrikes() {
        StatsProcessing.putTogetherTracksOfSameStrikes(trackDataList);
        assertEquals(2, trackDataList.size());
        // try again, no change expected
        StatsProcessing.putTogetherTracksOfSameStrikes(trackDataList);
        assertEquals(2, trackDataList.size());
    }

    @Test
    public void averageZPositions() {
        trackDataList = new LinkedList<>();
        detectionDataArr = new DetectionData[]{
                new DetectionData(10, 10, 1, 100f, false, DirectionX.RIGHT),
                new DetectionData(20, 10, 2, 200f, false, DirectionX.RIGHT),
                new DetectionData(30, 10, 5, 300f, false, DirectionX.RIGHT),
        };
        List<DetectionData> detectionDataList = new LinkedList<>();
        detectionDataList.add(detectionDataArr[0]);
        detectionDataList.add(detectionDataArr[1]);
        detectionDataList.add(detectionDataArr[2]);
        List<TrackData> trackDataList = new LinkedList<>();
        trackDataList.add(new TrackData(detectionDataList, 100f, Side.LEFT));
        StatsProcessing.averageZPositions(trackDataList);
        assertNotEquals(1.0, trackDataList.get(0).getDetections().get(0).getZ());
        assertNotEquals(2.0, trackDataList.get(0).getDetections().get(1).getZ());
        assertNotEquals(5.0, trackDataList.get(0).getDetections().get(2).getZ());
        assertEquals(3, trackDataList.get(0).getDetections().size());
    }

    @Test
    public void findFastestStrikeOfBothSides() {
        Map<Side, Float> fastestStrikes = StatsProcessing.findFastestStrikeOfBothSides(trackDataList);
        assertEquals(200f, fastestStrikes.get(Side.RIGHT), 0f);
        assertEquals(300f, fastestStrikes.get(Side.LEFT), 0f);
    }

    @Test
    public void countStrikes() {
        Map<Side, Integer> strikes = StatsProcessing.countAmountOfStrikesOfBothSides(trackDataList);
        assertEquals(2, (int) strikes.get(Side.RIGHT));
        assertEquals(3, (int) strikes.get(Side.LEFT));
    }
}