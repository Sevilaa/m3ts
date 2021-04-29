package ch.m3ts.display.stats;

import java.io.Serializable;
import java.util.List;

import ch.m3ts.tabletennis.helper.Side;

public class TrackData implements Serializable {
    private final List<DetectionData> detections;
    private final Side striker;
    private float averageVelocity;

    public TrackData(List<DetectionData> detections, float averageVelocity, Side striker) {
        this.detections = detections;
        this.averageVelocity = averageVelocity;
        this.striker = striker;
    }

    public float getAverageVelocity() {
        return averageVelocity;
    }

    public void setAverageVelocity(float averageVelocity) {
        this.averageVelocity = averageVelocity;
    }

    public Side getStriker() {
        return striker;
    }

    public List<DetectionData> getDetections() {
        return detections;
    }
}
