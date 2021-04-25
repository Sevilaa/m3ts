package ch.m3ts.display.stats;

import java.io.Serializable;

public class DetectionData implements Serializable {
    private final int x;
    private final int y;
    private final int z;
    private final float velocity;
    private final boolean wasBounce;

    public DetectionData(int x, int y, int z, float velocity, boolean wasBounce) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocity = velocity;
        this.wasBounce = wasBounce;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public float getVelocity() {
        return velocity;
    }

    public boolean wasBounce() {
        return wasBounce;
    }
}
