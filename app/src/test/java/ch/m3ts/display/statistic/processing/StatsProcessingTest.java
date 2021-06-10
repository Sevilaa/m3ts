package ch.m3ts.display.statistic.processing;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.m3ts.display.statistic.data.DetectionData;
import ch.m3ts.display.statistic.data.TrackData;
import ch.m3ts.util.DirectionX;
import ch.m3ts.util.Side;

import static org.junit.Assert.assertEquals;

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
        trackDataList.get(0).getDetections().add(new DetectionData(9, 10, 1, 100f, false, DirectionX.RIGHT));
        trackDataList.get(1).getDetections().add(new DetectionData(19, 10, 1, 100f, false, DirectionX.RIGHT));
        trackDataList.get(2).getDetections().add(new DetectionData(29, 10, 1, 100f, false, DirectionX.RIGHT));
        trackDataList.get(3).getDetections().add(new DetectionData(26, 10, 1, 100f, false, DirectionX.LEFT));
        trackDataList.get(4).getDetections().add(new DetectionData(16, 10, 1, 100f, false, DirectionX.LEFT));
        StatsProcessing.putTogetherTracksOfSameStrikes(trackDataList);
        assertEquals(2, trackDataList.size());
        // try again, no change expected
        StatsProcessing.putTogetherTracksOfSameStrikes(trackDataList);
        assertEquals(2, trackDataList.size());
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