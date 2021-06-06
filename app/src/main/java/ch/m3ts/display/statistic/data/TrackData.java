package ch.m3ts.display.statistic.data;

import java.io.Serializable;
import java.util.List;

import ch.m3ts.util.Side;

public class TrackData implements Serializable {
    private List<DetectionData> detections;
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

    public void setDetections(List<DetectionData> detections) {
        this.detections = detections;
    }
}
