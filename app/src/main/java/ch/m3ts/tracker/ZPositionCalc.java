package ch.m3ts.tracker;

import android.support.annotation.NonNull;

import java.util.TreeSet;

import ch.m3ts.util.Log;

/**
 * Calculates the Z-Position of the Table Tennis Ball from the radius detected by FMO and
 * the selected table by the user.
 */
public class ZPositionCalc {
    public static final double TABLE_TENNIS_TABLE_LENGTH_MM = 2740; // normed, see https://en.wikipedia.org/wiki/Table_tennis
    public static final double TABLE_TENNIS_TABLE_WIDTH_MM = 1525; // normed, see https://en.wikipedia.org/wiki/Table_tennis
    public static final int MAX_OFFSET_MM = 100;
    private static final double TABLE_TENNIS_BALL_DIAMETER_MM = 40; // normed, see https://www.sport-thieme.ch/Tischtennisb%C3%A4lle#:~:text=Ein%20klassischer%20Tischtennisball%20hat%20einen,Zelluloid%20und%20ist%20innen%20hohl.
    private static final int ACCURACY = 500;
    private TreeSet<RadiusToZPosObj> radiusToZPosObjTreeSet;
    private final double horizontalViewAngle;
    private final double videoWidthMM;
    private final double videoWidthBackEdgeMM;
    private final double distanceTrackerToTableFrontEdgeMM;
    private final double distanceTrackerToTableBackEdgeMM;
    private final double ballRadiusFrontEdgePx;
    private final double ballRadiusBackEdgePx;
    private final double pxPerMMFrontEdge;
    private final double pxPerMMBackEdge;
    private final double mmPerPixelFrontEdge;
    private final double mmPerPixelBackEdge;

    public ZPositionCalc(double horizontalViewAngle, int tableLengthPixel, int videoWidthPixel) {
        this.mmPerPixelFrontEdge = TABLE_TENNIS_TABLE_LENGTH_MM / tableLengthPixel;
        this.pxPerMMFrontEdge = Math.pow(this.mmPerPixelFrontEdge, -1.0);           // inverse
        this.videoWidthMM = this.mmPerPixelFrontEdge * videoWidthPixel;
        this.horizontalViewAngle = horizontalViewAngle / 2.0;
        double a = videoWidthMM / 2.0;
        double c = a / Math.sin(Math.toRadians(this.horizontalViewAngle));
        double b = Math.sqrt(Math.pow(c, 2) - Math.pow(a, 2));
        this.distanceTrackerToTableFrontEdgeMM = b;
        this.distanceTrackerToTableBackEdgeMM = b + TABLE_TENNIS_TABLE_WIDTH_MM;
        Log.d("Distance to table front: " + distanceTrackerToTableFrontEdgeMM + "mm");
        Log.d("Distance to table back: " + distanceTrackerToTableBackEdgeMM + "mm");

        this.videoWidthBackEdgeMM = (Math.tan(Math.toRadians(this.horizontalViewAngle)) * distanceTrackerToTableBackEdgeMM) * 2;
        Log.d("Width Front Side: " + videoWidthMM + "mm");
        Log.d("Width Back Edge: " + videoWidthBackEdgeMM + "mm");

        this.mmPerPixelBackEdge = this.mmPerPixelFrontEdge * (this.videoWidthBackEdgeMM / this.videoWidthMM);       // should scale linearly
        this.pxPerMMBackEdge = Math.pow(this.mmPerPixelBackEdge, -1.0);
        ballRadiusFrontEdgePx = (pxPerMMFrontEdge * TABLE_TENNIS_BALL_DIAMETER_MM) / 2;
        ballRadiusBackEdgePx = (pxPerMMBackEdge * TABLE_TENNIS_BALL_DIAMETER_MM) / 2;
        Log.d("Ball radius between: " + ballRadiusFrontEdgePx + "px and " + ballRadiusBackEdgePx + "px");
        fillRadiusToZPosTree();
    }

    public double getMmPerPixelFrontEdge() {
        return mmPerPixelFrontEdge;
    }

    private void fillRadiusToZPosTree() {
        this.radiusToZPosObjTreeSet = new TreeSet<>();
        double step = (TABLE_TENNIS_TABLE_WIDTH_MM + MAX_OFFSET_MM * 2) / (double) ACCURACY;
        double closestLine = this.distanceTrackerToTableFrontEdgeMM - MAX_OFFSET_MM;
        for (int i = 0; i < ACCURACY; i++) {
            double b = closestLine + i * step;
            double a = (Math.tan(Math.toRadians(this.horizontalViewAngle)) * b) * 2.0;
            double mmPerPixel = mmPerPixelFrontEdge * (a / this.videoWidthMM);       // should scale linearly
            double pxPerMM = Math.pow(mmPerPixel, -1.0);
            double ballRadiusPx = (pxPerMM * TABLE_TENNIS_BALL_DIAMETER_MM) / 2.0;
            double zPosMm = b - this.distanceTrackerToTableFrontEdgeMM;
            double zPosPx = zPosMm * pxPerMM;
            radiusToZPosObjTreeSet.add(new RadiusToZPosObj(ballRadiusPx, zPosMm, zPosPx, pxPerMM));
            if (i == 0) {
                Log.d("Ball has following radius on closest line: " + ballRadiusPx + "px (with zPos being: " + zPosMm + "mm)");
                Log.d("Closest line is: " + (distanceTrackerToTableFrontEdgeMM - b) + "mm behind Front Edge of Table");
            } else if (i == ACCURACY - 1) {
                Log.d("Ball has following radius on furthest line: " + ballRadiusPx + "px (with zPos being: " + zPosMm + "mm)");
                Log.d("Closest line is: " + (b - distanceTrackerToTableBackEdgeMM) + "mm behind Back Edge of Table");
            }
        }
    }

    public boolean isBallZPositionOnTable(double ballRadiusPx) {
        return (ballRadiusPx <= ballRadiusFrontEdgePx * 1.05);
    }

    public double findZPosOfBallMm(double ballRadiusPx) {
        RadiusToZPosObj obj = radiusToZPosObjTreeSet.higher(new RadiusToZPosObj(ballRadiusPx, 0, 0, 0));
        if (obj == null) {
            if (ballRadiusPx > ballRadiusFrontEdgePx) {
                obj = radiusToZPosObjTreeSet.last();
            } else {
                obj = radiusToZPosObjTreeSet.first();
            }
        }
        return obj.zPosMm + MAX_OFFSET_MM;
    }

    /**
     * Returns the z-Position of the ball relative to the table width.
     * Examples:
     * returns val = 0 => ball is on the closest edge of the table (with offset)
     * returns val = 1 => ball is on the furthest edge of the table (with offset)
     *
     * @param ballRadiusPx ball (Lib.Detection) radius in pixel
     * @return the z-Position of the ball relative to the table width.
     */
    public double findZPosOfBallRel(double ballRadiusPx) {
        return findZPosOfBallMm(ballRadiusPx) / (TABLE_TENNIS_TABLE_WIDTH_MM + 2 * MAX_OFFSET_MM);
    }

    public double[] getTableDistanceMM() {
        return new double[]{distanceTrackerToTableFrontEdgeMM, distanceTrackerToTableBackEdgeMM};
    }

    private class RadiusToZPosObj implements Comparable {
        private final float radius;
        private final double zPosMm;
        private final double zPosPx;
        private final double pxlToMM;

        RadiusToZPosObj(double radius, double zPosMm, double zPosPx, double pxlToMM) {
            this.radius = (float) radius;
            this.zPosMm = zPosMm;
            this.zPosPx = zPosPx;
            this.pxlToMM = pxlToMM;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            RadiusToZPosObj other = (RadiusToZPosObj) o;
            return (int) Math.signum(this.radius - other.radius);
        }
    }
}
