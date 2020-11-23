package cz.fmo.events;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.fmo.Lib;
import cz.fmo.data.Track;
import cz.fmo.data.TrackSet;
import cz.fmo.events.timeouts.TimeoutTimerTask;
import cz.fmo.tabletennis.Side;
import cz.fmo.tabletennis.Table;
import cz.fmo.util.Config;
import helper.DirectionX;
import helper.DirectionY;

public class EventDetector implements Lib.Callback {
    private static final double PERCENTAGE_OF_NEARLY_OUT_OF_FRAME = 0.07;
    private static final int MILLISECONDS_TILL_TIMEOUT = 1000;
    private final TrackSet tracks;
    private final List<EventDetectionCallback> callbacks;
    private final int[] nearlyOutOfFrameThresholds;
    private int srcWidth;
    private int srcHeight;
    private Lib.Detection previousDetection;
    private int previousDirectionY;
    private int previousDirectionX;
    private int previousCenterX;
    private int previousCenterY;
    private Side currentBallSide;
    private Table table;
    private Timer timeoutTimer;
    private int numberOfDetections;

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
        this.numberOfDetections = 0;
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
        tracks.addDetections(detections, this.srcWidth, this.srcHeight, detectionTime); // after this, object direction is up to date
        if (!tracks.getTracks().isEmpty()) {
            numberOfDetections++;
            Track track = selectTrack(tracks.getTracks());
            if (track != null) {
                Lib.Detection latestDetection = track.getLatest();
                calcDirectionY(latestDetection);
                calcDirectionX(latestDetection);
                if (hasBallFallenOffSideWays(latestDetection)){
                    callAllOnBallDroppedSideWays();
                }
                callAllOnStrikeFound(tracks);
                hasSideChanged(latestDetection);
                hasBouncedOnTable(latestDetection);
                hasTableSideChanged(latestDetection.centerX);
                Side nearlyOutOfFrameSide = getNearlyOutOfFrameSide(latestDetection);
                if (nearlyOutOfFrameSide != null) {
                    callAllOnNearlyOutOfFrame(latestDetection, nearlyOutOfFrameSide);
                }
                savePreviousDetection(latestDetection);
            }
            setTimeoutTimer(numberOfDetections);
        }
    }

    public Track selectTrack(List<Track> tracks) {
        // first tag all tracks which have crossed the table once
        for(Track t : tracks) {
            Lib.Detection latestDetection = t.getLatest();
            if (table.isOnOrAbove(latestDetection.centerX, latestDetection.centerY)) {
                t.setTableCrossed();
            }
        }

        // now select a track which has crossed the table, preferably oldest one (index low), if there are none return null
        Track selectedTrack = null;
        for(Track t : tracks) {
            if (isOnTable(t)) {
               selectedTrack = t;
               break;
            }
        }
        return selectedTrack;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public int[] getNearlyOutOfFrameThresholds() {
        return nearlyOutOfFrameThresholds;
    }

    public void callAllOnTimeout() {
        for (EventDetectionCallback callback : callbacks) {
            callback.onTimeout();
        }
    }

    public int getNumberOfDetections() {
        return numberOfDetections;
    }

    private void setTimeoutTimer(int currentNumberOfDetections) {
        TimerTask timeoutTimerTask = new TimeoutTimerTask(this, currentNumberOfDetections);
        this.timeoutTimer = new Timer("timeoutTimer");
        this.timeoutTimer.schedule(timeoutTimerTask, MILLISECONDS_TILL_TIMEOUT);
    }

    private void callAllOnStrikeFound(TrackSet tracks) {
        for (EventDetectionCallback callback : callbacks) {
            callback.onStrikeFound(tracks);
        }
    }

    private void savePreviousDetection(Lib.Detection detection) {
        // important check, if removed dirX and dirY will be set to 0 sometimes
        if (detection != this.previousDetection) {
            this.previousCenterX = detection.centerX;
            this.previousCenterY = detection.centerY;
            this.previousDirectionX = (int) detection.directionX;
            this.previousDirectionY = (int) detection.directionY;
            this.previousDetection = detection;
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

    private void callAllOnBallDroppedSideWays() {
        for (EventDetectionCallback callback : callbacks) {
            callback.onBallDroppedSideWays();
        }
    }

    private void hasSideChanged(Lib.Detection detection) {
        Side otherBallSide = Side.RIGHT;
        if(currentBallSide == Side.RIGHT) otherBallSide = Side.LEFT;
        if ((detection.directionX == DirectionX.LEFT && currentBallSide != Side.RIGHT) ||
                (detection.directionX == DirectionX.RIGHT && currentBallSide != Side.LEFT)) {
            currentBallSide = otherBallSide;
            callAllOnSideChange(otherBallSide);
        }
    }

    private void hasBouncedOnTable(Lib.Detection detection) {
        if (previousDirectionY != detection.directionY &&
                (previousDirectionX == detection.directionX) &&
                (table.isBounceOn(previousCenterX, previousCenterY) || table.isBounceOn(detection.centerX, detection.centerY)) &&
                ((previousDirectionY == DirectionY.DOWN) && (detection.directionY == DirectionY.UP))) {
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

    private void calcDirectionY(Lib.Detection detection) {
        detection.directionY = Integer.compare(detection.centerY, previousCenterY);
    }

    private void calcDirectionX(Lib.Detection detection) {
        detection.directionX = Integer.compare(detection.centerX, previousCenterX);
    }

    private void hasTableSideChanged(int currentXPosition) {
        if (previousDetection != null) {
            if (currentXPosition > table.getCloseNetEnd().x && previousCenterX < table.getCloseNetEnd().x) {
                callAllOnTableSideChange(Side.RIGHT);
            } else if (currentXPosition < table.getCloseNetEnd().x && previousCenterX > table.getCloseNetEnd().x) {
                callAllOnTableSideChange(Side.LEFT);
            }
        }
    }

    private boolean hasBallFallenOffSideWays(Lib.Detection detection) {
        return (detection.predecessor != null &&
                table.isBelow(detection.centerX, detection.centerY) &&
                detection.directionY == DirectionY.DOWN);
    }
}