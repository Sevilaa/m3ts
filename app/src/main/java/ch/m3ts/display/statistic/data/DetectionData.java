package ch.m3ts.display.statistic.data;

import java.io.Serializable;

public class DetectionData implements Serializable {
    private final int x;
    private final int y;
    private double z;
    private final int directionX;
    private final float velocity;
    private final boolean wasBounce;

    public DetectionData(int x, int y, double z, float velocity, boolean wasBounce, int directionX) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocity = velocity;
        this.wasBounce = wasBounce;
        this.directionX = directionX;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getVelocity() {
        return velocity;
    }

    public boolean wasBounce() {
        return wasBounce;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getDirectionX() {
        return directionX;
    }
}
