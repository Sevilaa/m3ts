package cz.fmo.data;

import ch.m3ts.util.Side;
import cz.fmo.Lib;
import cz.fmo.util.Color;
import cz.fmo.util.Config;

/**
 * A series of objects detected in consecutive frames that are considered to be the same object
 * captured at different times.
 */
public class Track {
    public static final float MAX_VELOCITY_POSSIBLE_M_S = 31.3f;
    private static final float MAX_VELOCITY_POSSIBLE_KM_H = 112.654f;
    private static final float MAX_VELOCITY_POSSIBLE_MPH = 70f;
    private final Config mConfig;
    private Lib.Detection mLatest;
    private float mLatestDx = 0;
    private float mLatestDy = 0;
    private final Color.HSV mColorHSV = new Color.HSV();
    private final Color.RGBA mColorRGBA = new Color.RGBA();
    private long mLastDetectionTime;
    private float mMaxVelocity;
    private float mAvgVelocity;
    private float mSumVelocity;
    private int mVelocityNumFrames = 0;
    private boolean hasCrossedTable = false;
    private boolean ignore = false;
    private boolean isZPosOnTable = false;
    private Side striker;

    Track(Config config) {
        mConfig = config;
    }

    public Lib.Detection getLatest() {
        return mLatest;
    }

    public Color.RGBA getColor() {
        return mColorRGBA;
    }

    public long getLastDetectionTime() {
        return mLastDetectionTime;
    }

    public void setTableCrossed() {
        this.hasCrossedTable = true;
        this.isZPosOnTable = true;
    }

    public void setIgnore() {
        this.ignore = true;
    }

    public void setStriker(Side striker) {
        this.striker = striker;
    }

    public Side getStriker() {
        return this.striker;
    }

    public boolean hasCrossedTable() {
        return this.hasCrossedTable && this.isZPosOnTable && !ignore;
    }

    void setLatest(Lib.Detection latest, long detectionTime) {
        if (mLatest != null) {
            // calculate speed stats for each segment
            mLatestDx = (float) latest.centerX - mLatest.centerX;
            mLatestDy = (float) latest.centerY - mLatest.centerY;

            latest.directionY = mLatestDy / Math.abs(mLatestDy); // 1 => object is going down | -1 => object going up
            latest.directionX = mLatestDx / Math.abs(mLatestDx); // -1 => object going left | 1 => object going right

            float velocity = latest.velocity;

            // for real-world estimation, apply a formula
            if (mConfig.getVelocityEstimationMode() != Config.VelocityEstimationMode.PX_FR) {
                velocity *= (mConfig.getObjectRadius() / latest.radius) * mConfig.getFrameRate();
            }

            // convert m/s to other units
            switch (mConfig.getVelocityEstimationMode()) {
                case M_S:
                    velocity = Math.min(velocity, MAX_VELOCITY_POSSIBLE_M_S);
                    break;
                case KM_H:
                    velocity *= 3.6f;
                    velocity = Math.min(velocity, MAX_VELOCITY_POSSIBLE_KM_H);
                    break;
                case MPH:
                    velocity *= 2.23694f;
                    velocity = Math.min(velocity, MAX_VELOCITY_POSSIBLE_MPH);
                    break;
                default:
                    break;
            }
            ++mVelocityNumFrames;
            mSumVelocity += velocity;
            mMaxVelocity = Math.max(velocity, mMaxVelocity);
            mAvgVelocity = mSumVelocity / mVelocityNumFrames;
        } else {
            latest.directionY = 0;
            latest.directionX = 0;
        }

        mLastDetectionTime = detectionTime;
        mLatest = latest;
    }

    public float getAvgVelocity() {
        return mAvgVelocity;
    }

    public float getMaxVelocity() {
        return mMaxVelocity;
    }

    public void updateColor() {
        if (mLatestDx == 0 && mLatestDy == 0) return;
        float sinceDetectionSec = ((float) (System.nanoTime() - mLastDetectionTime)) / 1e9f;
        mColorHSV.hsv[0] = (mLatestDx > 0) ? 100.f : 200.f;
        mColorHSV.hsv[1] = Math.min(1.0f, .2f + 0.4f * sinceDetectionSec);
        mColorHSV.hsv[2] = Math.max((mLatestDx > 0) ? 0.6f : 0.8f, 1.f - 0.3f * sinceDetectionSec);
        Color.convert(mColorHSV, mColorRGBA);
    }
}
