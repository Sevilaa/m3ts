package ch.m3ts.detection;

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
    private TreeSet<ZPosMmToProportion> proportionTreeSet;
    private final double horizontalViewAngle;
    private final double videoWidthPx;
    private final double distanceTrackerToTableFrontEdgeMM;
    private final double distanceTrackerToTableBackEdgeMM;
    private final double ballRadiusFrontEdgePx;
    private final double mmPerPixelFrontEdge;

    public ZPositionCalc(double horizontalViewAngle, int tableLengthPixel, int videoWidthPixel) {
        this.videoWidthPx = videoWidthPixel;
        this.mmPerPixelFrontEdge = TABLE_TENNIS_TABLE_LENGTH_MM / tableLengthPixel;
        double pxPerMMFrontEdge = Math.pow(this.mmPerPixelFrontEdge, -1.0);           // inverse
        double videoWidthMM = this.mmPerPixelFrontEdge * videoWidthPixel;
        this.horizontalViewAngle = horizontalViewAngle / 2.0;
        double a = videoWidthMM / 2.0;
        double c = a / Math.sin(Math.toRadians(this.horizontalViewAngle));
        double b = Math.sqrt(Math.pow(c, 2) - Math.pow(a, 2));
        this.distanceTrackerToTableFrontEdgeMM = b;
        this.distanceTrackerToTableBackEdgeMM = b + TABLE_TENNIS_TABLE_WIDTH_MM;
        Log.d("Distance to table front: " + distanceTrackerToTableFrontEdgeMM + "mm");
        Log.d("Distance to table back: " + distanceTrackerToTableBackEdgeMM + "mm");

        double videoWidthBackEdgeMM = (Math.tan(Math.toRadians(this.horizontalViewAngle)) * distanceTrackerToTableBackEdgeMM) * 2;
        Log.d("Width Front Side: " + videoWidthMM + "mm");
        Log.d("Width Back Edge: " + videoWidthBackEdgeMM + "mm");

        double mmPerPixelBackEdge = this.mmPerPixelFrontEdge * (videoWidthBackEdgeMM / videoWidthMM);       // should scale linearly
        double pxPerMMBackEdge = Math.pow(mmPerPixelBackEdge, -1.0);
        ballRadiusFrontEdgePx = (pxPerMMFrontEdge * TABLE_TENNIS_BALL_DIAMETER_MM) / 2;
        double ballRadiusBackEdgePx = (pxPerMMBackEdge * TABLE_TENNIS_BALL_DIAMETER_MM) / 2;
        Log.d("Ball radius between: " + ballRadiusFrontEdgePx + "px and " + ballRadiusBackEdgePx + "px");
        fillRadiusToZPosTree(videoWidthPixel);
    }

    public double getMmPerPixelFrontEdge() {
        return mmPerPixelFrontEdge;
    }

    private void fillRadiusToZPosTree(int videoWidthPx) {
        this.radiusToZPosObjTreeSet = new TreeSet<>();
        this.proportionTreeSet = new TreeSet<>();
        double step = (TABLE_TENNIS_TABLE_WIDTH_MM + MAX_OFFSET_MM * 2) / (double) ACCURACY;
        double closestLine = this.distanceTrackerToTableFrontEdgeMM - MAX_OFFSET_MM;
        for (int i = 0; i < ACCURACY; i++) {
            double b = closestLine + i * step;
            double a = (Math.tan(Math.toRadians(this.horizontalViewAngle)) * b) * 2.0;
            double mmPerPxX = a / videoWidthPx;
            double pxPerMM = Math.pow(mmPerPxX, -1.0);
            double ballRadiusPx = (pxPerMM * TABLE_TENNIS_BALL_DIAMETER_MM) / 2.0;
            double zPosMm = i * step;
            radiusToZPosObjTreeSet.add(new RadiusToZPosObj(ballRadiusPx, zPosMm));
            proportionTreeSet.add(new ZPosMmToProportion(zPosMm, mmPerPxX));
            if (i == 0) {
                Log.d("Ball has following radius on closest line: " + ballRadiusPx + "px (with zPos being: " + zPosMm + "mm)");
                Log.d("Closest line is: " + (distanceTrackerToTableFrontEdgeMM - b) + "mm behind Front Edge of Table");
            } else if (i == ACCURACY - 1) {
                Log.d("Ball has following radius on furthest line: " + ballRadiusPx + "px (with zPos being: " + zPosMm + "mm)");
                Log.d("Closest line is: " + (b - distanceTrackerToTableBackEdgeMM) + "mm behind Back Edge of Table");
            }
        }
    }

    // checks if the ball radius is too big for it to be on the table
    public boolean isBallZPositionOnTable(double ballRadiusPx) {
        return (ballRadiusPx <= ballRadiusFrontEdgePx * 1.05);
    }

    public double findZPosOfBallMm(double ballRadiusPx) {
        RadiusToZPosObj obj = radiusToZPosObjTreeSet.higher(new RadiusToZPosObj(ballRadiusPx, 0));
        if (obj == null) {
            if (ballRadiusPx > ballRadiusFrontEdgePx) {
                obj = radiusToZPosObjTreeSet.last();
            } else {
                obj = radiusToZPosObjTreeSet.first();
            }
        }
        return obj.zPosMm;
    }

    public ZPosMmToProportion findProportionOfZPos(double zPosRel) {
        double zPosMm = zPosRelToMm(zPosRel);
        ZPosMmToProportion proportion = proportionTreeSet.lower(new ZPosMmToProportion(zPosMm, 0));
        if (proportion == null) {
            if (zPosRel >= 0.5) {
                proportion = proportionTreeSet.last();
            } else {
                proportion = proportionTreeSet.first();
            }
        }
        return proportion;
    }

    /**
     * Returns the z-Position of the ball relative to the table width.
     * (z-Position <= 1 && z-Position >= 0)
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

    public double zPosRelToMm(double zPosRel) {
        return zPosRel * (TABLE_TENNIS_TABLE_WIDTH_MM + 2 * MAX_OFFSET_MM);
    }

    public double[] getTableDistanceMM() {
        return new double[]{distanceTrackerToTableFrontEdgeMM, distanceTrackerToTableBackEdgeMM};
    }

    public double getVideoWidthPx() {
        return videoWidthPx;
    }

    private class RadiusToZPosObj implements Comparable {
        private final float radius;
        private final double zPosMm;

        RadiusToZPosObj(double radius, double zPosMm) {
            this.radius = (float) radius;
            this.zPosMm = zPosMm;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            RadiusToZPosObj other = (RadiusToZPosObj) o;
            return (int) Math.signum(this.radius - other.radius);
        }
    }

    public class ZPosMmToProportion implements Comparable {
        private final double zPosMm;
        private final double proportion;

        public ZPosMmToProportion(double zPosMm, double proportion) {
            this.zPosMm = zPosMm;
            this.proportion = proportion;
        }

        public double getProportion() {
            return proportion;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            ZPosMmToProportion other = (ZPosMmToProportion) o;
            return Double.compare(this.zPosMm, other.zPosMm);
        }
    }
}
