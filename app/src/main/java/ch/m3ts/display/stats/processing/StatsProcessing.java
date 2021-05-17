package ch.m3ts.display.stats.processing;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.m3ts.display.stats.DetectionData;
import ch.m3ts.display.stats.TrackData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tracker.ZPositionCalc;
import edu.princeton.cs.algs4.LinearRegression;

public class StatsProcessing {
    private static final double FRAME_RATE = 30.0;

    private StatsProcessing() {
    }

    public static void recalculateVelocity(List<TrackData> trackDataList, Map<Side, Integer> tableCorners) {
        int leftCorner = tableCorners.get(Side.LEFT);
        int rightCorner = tableCorners.get(Side.RIGHT);
        int widthPx = rightCorner - leftCorner;
        double mmPerPx = ZPositionCalc.TABLE_TENNIS_TABLE_LENGTH_MM / widthPx;
        for (TrackData trackData : trackDataList) {
            DetectionData lastDetection = trackData.getDetections().get(0);
            DetectionData firstDetection = trackData.getDetections().get(trackData.getDetections().size() - 1);
            if (lastDetection == firstDetection || trackData.getDetections().size() == 1) {
                trackData.setAverageVelocity(0);
            } else {
                double dx = Math.abs(lastDetection.getX() - firstDetection.getX()) * mmPerPx;
                double dy = Math.abs(lastDetection.getY() - firstDetection.getY()) * mmPerPx;
                double dz = Math.abs(lastDetection.getZ() - firstDetection.getZ()) * (ZPositionCalc.TABLE_TENNIS_TABLE_WIDTH_MM
                        + 2 * ZPositionCalc.MAX_OFFSET_MM);
                double distanceInMm = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2) + Math.pow(dz, 2));
                double distanceInM = distanceInMm / 1000.0;
                double dTimeInS = (1 / FRAME_RATE) * (trackData.getDetections().size() - 1);
                float velocityMPerS = (float) (distanceInM / dTimeInS);
                float velocityKmPerH = velocityMPerS * 3.6f;
                trackData.setAverageVelocity(velocityKmPerH);
            }
        }
    }

    /**
     * "Glues" together multiple tracks of the same strike.
     * Basically, if 2 tracks share the same xDirection (f.e. both going to the left),
     * they're most likely from the same strike.
     *
     * @param tracks list of all tracks (f.e. of a point)
     */
    public static void putTogetherTracksOfSameStrikes(List<TrackData> tracks) {
        Iterator<TrackData> trackDataIterator = tracks.iterator();
        TrackData lastTrackData = null;
        while (trackDataIterator.hasNext()) {
            TrackData nextTrackData = trackDataIterator.next();
            if (lastTrackData != null) {
                DetectionData lastDetection = nextTrackData.getDetections().get(nextTrackData.getDetections().size() - 1);
                DetectionData lastDetectionPrev = lastTrackData.getDetections().get(lastTrackData.getDetections().size() - 1);
                if (lastDetection.getDirectionX() == lastDetectionPrev.getDirectionX()) {
                    // likely same track, append detections and remove
                    lastTrackData.getDetections().addAll(nextTrackData.getDetections());

                    // re-average the velocity
                    int totalNDetections = lastTrackData.getDetections().size() + nextTrackData.getDetections().size();
                    float weightedVelocityLast = lastTrackData.getAverageVelocity() * ((float) lastTrackData.getDetections().size() / totalNDetections);
                    float weightedVelocityNext = nextTrackData.getAverageVelocity() * ((float) nextTrackData.getDetections().size() / totalNDetections);
                    lastTrackData.setAverageVelocity(weightedVelocityLast + weightedVelocityNext);

                    trackDataIterator.remove();
                    nextTrackData = lastTrackData;
                }
            }
            lastTrackData = nextTrackData;
        }
    }

    /**
     * Averages the Z-Position of all tracks / detection using linear regression.
     *
     * @param tracks List of all tracks (f.e. of a point)
     */
    public static void averageZPositions(List<TrackData> tracks) {
        for (TrackData track : tracks) {
            List<DetectionData> detections = track.getDetections();
            double[] x = new double[detections.size()];
            double[] z = new double[detections.size()];

            for (int i = 0; i < detections.size(); i++) {
                x[i] = detections.get(i).getX();
                z[i] = detections.get(i).getZ();
            }

            LinearRegression linearRegression = new LinearRegression(x, z);
            for (int i = 0; i < detections.size(); i++) {
                DetectionData detection = detections.get(i);
                detection.setZ(linearRegression.predict(detection.getX()));
            }
        }
    }

    /**
     * Finds the fastest strike/track of both players (Side.LEFT & Side.RIGHT).
     *
     * @param tracks list of all tracks (f.e. of a point)
     * @return Map with values of the fastest strike of all tracks per side
     */
    public static Map<Side, Float> findFastestStrikeOfBothSides(List<TrackData> tracks) {
        Map<Side, Float> fastestStrikes = new EnumMap<>(Side.class);
        fastestStrikes.put(Side.LEFT, 0f);
        fastestStrikes.put(Side.RIGHT, 0f);
        for (TrackData track : tracks) {
            if (track == null || fastestStrikes.get(track.getStriker()) == null) continue;
            if (track.getAverageVelocity() > fastestStrikes.get(track.getStriker()))
                fastestStrikes.put(track.getStriker(), track.getAverageVelocity());
        }
        return fastestStrikes;
    }

    /**
     * Counts the amount of strikes per side.
     *
     * @param tracks list of all tracks (f.e. of a point)
     * @return Map with values of the amount of strikes per side
     */
    public static Map<Side, Integer> countAmountOfStrikesOfBothSides(List<TrackData> tracks) {
        Map<Side, Integer> strikes = new HashMap<>();
        strikes.put(Side.LEFT, 0);
        strikes.put(Side.RIGHT, 0);
        for (TrackData track : tracks) {
            strikes.put(track.getStriker(), strikes.get(track.getStriker()) + 1);
        }
        return strikes;
    }
}
