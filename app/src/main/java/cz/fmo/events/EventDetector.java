package cz.fmo.events;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import cz.fmo.Lib;
import cz.fmo.data.Track;
import cz.fmo.data.TrackSet;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.Table;
import cz.fmo.util.Config;
import helper.DirectionX;
import helper.DirectionY;

public class EventDetector implements Lib.Callback {
    private static final int SIDE_CHANGE_DETECTION_SPEED = 2;
    private static final double PERCENTAGE_OF_NEARLY_OUT_OF_FRAME = 0.1;
    private final TrackSet tracks;
    private final List<EventDetectionCallback> callbacks;
    private final int[] nearlyOutOfFrameThresholds;
    private int srcWidth;
    private int srcHeight;
    private long detectionCount;
    private Lib.Detection previousDetection;
    private Lib.Detection beforePreviousDetection;
    private Table table;

    public EventDetector(Config config, int srcWidth, int srcHeight, EventDetectionCallback callback, TrackSet tracks, @NonNull Table table) {
        this(config, srcWidth, srcHeight, Collections.singletonList(callback), tracks, table);
    }

    public EventDetector(Config config, int srcWidth, int srcHeight, List<EventDetectionCallback> callbacks, TrackSet tracks, @NonNull Table table) {
        this.srcHeight = srcHeight;
        this.srcWidth = srcWidth;
        this.tracks = tracks;
        this.nearlyOutOfFrameThresholds = new int[] {
                (int) (srcWidth*PERCENTAGE_OF_NEARLY_OUT_OF_FRAME),
                (int) (srcWidth*(1-PERCENTAGE_OF_NEARLY_OUT_OF_FRAME)),
                (int) (srcHeight*PERCENTAGE_OF_NEARLY_OUT_OF_FRAME),
                (int) (srcHeight*(1-PERCENTAGE_OF_NEARLY_OUT_OF_FRAME)),
        };
        this.callbacks = callbacks;
        this.table = table;
        tracks.setConfig(config);
    }

    @Override
    public void log(String message) {
        // Lib logs will be ignored for now
    }

    @Override
    public void onObjectsDetected(Lib.Detection[] detections) {
        this.onObjectsDetected(detections, System.nanoTime());
    }

    public void onObjectsDetected(Lib.Detection[] detections, long detectionTime) {
        detectionCount++;
        tracks.addDetections(detections, this.srcWidth, this.srcHeight, detectionTime); // after this, object direction is updated

        if (tracks.getTracks().size() == 1) {
            Track track = tracks.getTracks().get(0);
            Lib.Detection latestDetection = track.getLatest();

            if (table.isOnOrAbove(latestDetection.centerX, latestDetection.centerY)) {
                track.setTableCrossed();
            }

            if (isOnTable(track)) {
                callAllOnStrikeFound(tracks);
                boolean sideChanged = false;
                if (beforePreviousDetection == null)
                    beforePreviousDetection = latestDetection;
                if (detectionCount % SIDE_CHANGE_DETECTION_SPEED == 0) {
                    sideChanged = hasSideChanged(latestDetection.directionX);
                    beforePreviousDetection = latestDetection;
                }
                if (!sideChanged)
                    hasBouncedOnTable(latestDetection);
                hasTableSideChanged(latestDetection.centerX);
                if (latestDetection.predecessor != null) {
                    previousDetection = latestDetection;
                }
            }

            Side nearlyOutOfFrameSide = getNearlyOutOfFrameSide(latestDetection);
            if (nearlyOutOfFrameSide != null) {
                callAllOnNearlyOutOfFrame(latestDetection, nearlyOutOfFrameSide);
            }
        }
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public int[] getNearlyOutOfFrameThresholds() {
        return nearlyOutOfFrameThresholds;
    }

    private void callAllOnStrikeFound(TrackSet tracks) {
        for (EventDetectionCallback callback : callbacks) {
            callback.onStrikeFound(tracks);
        }
    }

    private void callAllOnBounce(Lib.Detection latestDetection) {
        for (EventDetectionCallback callback : callbacks) {
            callback.onBounce(latestDetection);
        }
    }

    private void callAllOnSideChange(Side side) {
        for (EventDetectionCallback callback : callbacks) {
            callback.onSideChange(side);
        }
    }

    private void callAllOnNearlyOutOfFrame(Lib.Detection latestDetection, Side side) {
        for (EventDetectionCallback callback : callbacks) {
            callback.onNearlyOutOfFrame(latestDetection, side);
        }
    }

    private void callAllOnTableSideChange(Side side) {
        for (EventDetectionCallback callback : callbacks) {
            callback.onTableSideChange(side);
        }
    }

    private boolean hasSideChanged(float directionX) {
        boolean hasSideChanged = false;
        if (beforePreviousDetection != null && beforePreviousDetection.directionX != directionX) {
            Side side = Side.LEFT;
            if (directionX == DirectionX.LEFT) {
                side = Side.RIGHT;
            }
            callAllOnSideChange(side);
            hasSideChanged = true;
        }
        return hasSideChanged;
    }

    private void hasBouncedOnTable(Lib.Detection detection) {
        if (previousDetection != null && previousDetection.directionY > detection.directionY && previousDetection.directionX == detection.directionX
                && table.isOnOrAbove(detection.centerX, detection.centerY)) {
            callAllOnBounce(detection);
        }
    }

    private Side getNearlyOutOfFrameSide(Lib.Detection detection) {
        Side side = null;
        if (detection.predecessor != null) {
            if (detection.centerX < nearlyOutOfFrameThresholds[0] && detection.directionX == DirectionX.LEFT) {
                side = Side.LEFT;
            } else if(detection.centerX > nearlyOutOfFrameThresholds[1] && detection.directionX == DirectionX.RIGHT) {
                side = Side.RIGHT;
            } else if(detection.centerY < nearlyOutOfFrameThresholds[2] && detection.directionY == DirectionY.UP) {
                side = Side.TOP;
            } else if(detection.centerY > nearlyOutOfFrameThresholds[3] && detection.directionY == DirectionY.DOWN) {
                side = Side.BOTTOM;
            }
        }
        return side;
    }

    private boolean isOnTable(Track track) {
        return track.hasCrossedTable();
    }

    private void hasTableSideChanged(int currentXPosition) {
        if (previousDetection != null) {
            if (currentXPosition > table.getCloseNetEnd().x && previousDetection.centerX < table.getCloseNetEnd().x) {
                callAllOnTableSideChange(Side.RIGHT);
            } else if (currentXPosition < table.getCloseNetEnd().x && previousDetection.centerX > table.getCloseNetEnd().x) {
                callAllOnTableSideChange(Side.LEFT);
            }
        }
    }
}