package helper;

import java.util.Random;

import cz.fmo.Lib;
import cz.fmo.tabletennis.Side;

/**
 * Helper class which generates some Detections in various directions.
 */
public class DetectionGenerator {
    private static final int START_Y_POS = 100;
    private static final int START_X_POS = 600;
    private static final int SPACE_BETWEEN_OBJECTS = 50;
    private static final int MAX_ID = 99999;
    /**
     * Generates Detections which represent ONE object thrown horizontally.
     * The detections are generated such that they leap much into the middle of a 1920x1080 video frame
     * -> they will for sure cross the table.
     * @param toRight specifies if the "object" will move to the right, if false -> goes to left
     * @return 10 detections which form a horizontal line (with some space in between of course).
     */
    public static Lib.Detection[] makeDetectionsInXDirectionOnTable(boolean toRight) {
        int n = 10;
        int id = new Random().nextInt(MAX_ID);
        int directionFactor = -1;
        if(toRight) {directionFactor = 1;}

        Lib.Detection[] detections = new Lib.Detection[n];
        for (int i = 0; i<n; i++) {
            detections[i] = new Lib.Detection();
            detections[i].centerX = directionFactor*i*SPACE_BETWEEN_OBJECTS+START_X_POS;
            detections[i].centerY = START_Y_POS;
            detections[i].id = i + id;
            detections[i].radius = 3;
            detections[i].velocity = 20f;
            if (i > 0) {
                detections[i].predecessor = detections[i-1];
                detections[i].predecessorId = detections[i-1].id;
            }
        }
        return detections;
    }

    /**
     * Generates Detections at the top, right, bottom and left side nearly out of frame.
     * @param nearlyOutOfFrameThresholds specifies the threshold if an object is nearly out of frame.
     * @param sourceWidth specifies the width of the frame.
     * @param sourceHeight specifies the height of the frame.
     * @return detections which are nearly out of frame
     */
    public static Lib.Detection[] makeNearlyOutOfFrameDetections(int[] nearlyOutOfFrameThresholds, int sourceWidth, int sourceHeight) {
        Lib.Detection[] detections = new Lib.Detection[4];
        for(int i = 0; i < 4; i++) {
            detections[i] = new Lib.Detection();
        }
        detections[0].centerX = new Random().nextInt(nearlyOutOfFrameThresholds[0]);
        detections[0].centerY = new Random().nextInt(sourceHeight);
        detections[0].directionX = DirectionX.LEFT;
        detections[1].centerX = nearlyOutOfFrameThresholds[1]+1 + new Random().nextInt(nearlyOutOfFrameThresholds[0]);
        detections[1].centerY = new Random().nextInt(sourceHeight);
        detections[1].directionX = DirectionX.RIGHT;
        detections[2].centerX = new Random().nextInt(sourceWidth);
        detections[2].centerY = new Random().nextInt(nearlyOutOfFrameThresholds[2]);
        detections[2].directionY = DirectionY.UP;
        detections[3].centerX = new Random().nextInt(sourceWidth);
        detections[3].centerY = nearlyOutOfFrameThresholds[3]+1 + new Random().nextInt(nearlyOutOfFrameThresholds[2]);
        detections[3].directionY = DirectionY.DOWN;
        return detections;
    }

    public static Side getNearlyOutOfFrameSide(int[] nearlyOutOfFrameThresholds, Lib.Detection detection) {
        Side side = null;
        if (detection.centerX < nearlyOutOfFrameThresholds[0] && detection.directionX == DirectionX.LEFT) {
            side = Side.LEFT;
        } else if(detection.centerX > nearlyOutOfFrameThresholds[1] && detection.directionX == DirectionX.RIGHT) {
            side = Side.RIGHT;
        } else if(detection.centerY < nearlyOutOfFrameThresholds[2] && detection.directionY == DirectionY.UP) {
            side = Side.TOP;
        } else if(detection.centerY > nearlyOutOfFrameThresholds[3] && detection.directionY == DirectionY.DOWN) {
            side = Side.BOTTOM;
        }
        return side;
    }
}
