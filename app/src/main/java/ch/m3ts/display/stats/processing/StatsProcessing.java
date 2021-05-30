package ch.m3ts.display.stats.processing;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.m3ts.display.stats.data.DetectionData;
import ch.m3ts.display.stats.data.TrackData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tracker.ZPositionCalc;
import ch.m3ts.util.Log;
import edu.princeton.cs.algs4.LinearRegression;

public class StatsProcessing {
    private static final double FRAME_RATE = 30.0;
    public static final double OUTLIER_IGNORE_THRESHOLD = 0.99;

    private StatsProcessing() {
    }

    /**
     * Calculate velocity of a track using the 3D-Vector distance of first and last detection per track.
     *
     * @param trackDataList all tracks of which will have the velocity recalculated
     * @param calc          initialized ZPositionCalc
     */
    public static void recalculateVelocity(List<TrackData> trackDataList, ZPositionCalc calc) {
        for (TrackData trackData : trackDataList) {
            DetectionData lastDetection = trackData.getDetections().get(0);
            DetectionData firstDetection = trackData.getDetections().get(trackData.getDetections().size() - 1);
            if (lastDetection == firstDetection || trackData.getDetections().size() == 1 || calc == null) {
                trackData.setAverageVelocity(0);
            } else {
                ZPositionCalc.ZPosMmToProportion p1 = calc.findProportionOfZPos(firstDetection.getZ());
                ZPositionCalc.ZPosMmToProportion p2 = calc.findProportionOfZPos(lastDetection.getZ());
                double dx = Math.abs(lastDetection.getX() * p2.getpX() - firstDetection.getX() * p1.getpX());
                double dy = Math.abs(lastDetection.getY() * p2.getpX() - firstDetection.getY() * p1.getpX());
                double dz = Math.abs(lastDetection.getZ() - firstDetection.getZ()) * (ZPositionCalc.TABLE_TENNIS_TABLE_WIDTH_MM
                        + 2 * ZPositionCalc.MAX_OFFSET_MM);
                double distanceInMm = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2) + Math.pow(dz, 2));
                double distanceInM = distanceInMm / 1000.0;
                double dTimeInS = (1 / FRAME_RATE) * (trackData.getDetections().size() - 1);
                float velocityMPerS = (float) (distanceInM / dTimeInS);
                float velocityKmPerH = velocityMPerS * 3.6f;
                double[] xArr = new double[trackData.getDetections().size()];
                double[] yArr = new double[trackData.getDetections().size()];
                double[] zArr = new double[trackData.getDetections().size()];
                for (int i = 0; i < trackData.getDetections().size(); i++) {
                    DetectionData d = trackData.getDetections().get(i);
                    ZPositionCalc.ZPosMmToProportion p = calc.findProportionOfZPos(d.getZ());
                    xArr[i] = d.getX() * p.getpX();
                    yArr[i] = d.getY() * p.getpX();
                    zArr[i] = calc.zPosRelToMm(d.getZ());
                }
                trackData.setAverageVelocity(velocityKmPerH);
                Log.d("Calculating Velocity of track: \nxArr: " + Arrays.toString(xArr) +
                        "\nyArr: " + Arrays.toString(yArr) + "\nzArr: " + Arrays.toString(zArr) +
                        "\nx1: " + firstDetection.getX() * p1.getpX() + "mm x2: " + lastDetection.getX() * p2.getpX() + "mm\n" +
                        "y1: " + firstDetection.getY() * p1.getpX() + "mm y2: " + lastDetection.getY() * p2.getpX() + "mm\n" +
                        "z1: " + firstDetection.getZ() + " z2: " + lastDetection.getZ() + "\n" +
                        "velocity: " + velocityKmPerH + "km/h"
                );
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
                List<DetectionData> detections = nextTrackData.getDetections();
                int dir = Integer.compare(detections.get(detections.size() - 1).getX(), detections.get(0).getX());
                List<DetectionData> prevDetections = lastTrackData.getDetections();
                int dirPrev = Integer.compare(prevDetections.get(prevDetections.size() - 1).getX(), prevDetections.get(0).getX());
                if (dir == dirPrev) {
                    // likely same track, append detections and remove
                    Collections.reverse(lastTrackData.getDetections());
                    Collections.reverse(nextTrackData.getDetections());
                    lastTrackData.getDetections().addAll(nextTrackData.getDetections());
                    Collections.reverse(lastTrackData.getDetections());

                    // re-average the velocity
                    int totalNDetections = lastTrackData.getDetections().size() + nextTrackData.getDetections().size();
                    float weightedVelocityLast = lastTrackData.getAverageVelocity() * ((float) lastTrackData.getDetections().size() / totalNDetections);
                    float weightedVelocityNext = nextTrackData.getAverageVelocity() * ((float) nextTrackData.getDetections().size() / totalNDetections);
                    nextTrackData.setAverageVelocity(weightedVelocityLast + weightedVelocityNext);

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
            List<Double> x = new LinkedList<>();
            List<Double> z = new LinkedList<>();

            for (int i = 0; i < detections.size(); i++) {
                if (detections.get(i).getZ() < OUTLIER_IGNORE_THRESHOLD) {
                    x.add((double) detections.get(i).getX());
                    z.add(detections.get(i).getZ());
                }
            }

            if (!x.isEmpty() && !z.isEmpty()) {
                double[] xArr = new double[x.size()];
                double[] zArr = new double[z.size()];

                for (int i = 0; i < x.size(); i++) {
                    xArr[i] = x.get(i);
                    zArr[i] = z.get(i);
                }

                LinearRegression linearRegression = new LinearRegression(xArr, zArr);
                for (int i = 0; i < detections.size(); i++) {
                    DetectionData detection = detections.get(i);
                    detection.setZ(linearRegression.predict(detection.getX()));
                }
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
