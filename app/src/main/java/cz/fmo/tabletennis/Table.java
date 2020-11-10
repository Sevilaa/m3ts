package cz.fmo.tabletennis;

import android.graphics.Point;
import android.support.annotation.NonNull;

import java.util.Properties;

import cz.fmo.Lib;

public class Table {
    private Point[] corners;
    private Point[] net;

    public Table(@NonNull Point[] corners, @NonNull Point[] net) {
        if (corners.length != 4) {
           throw new NotFourCornersException(corners.length);
        }

        if (net.length != 4) {
            throw new NotFourNetCornersException(net.length);
        }
        this.corners = corners;
        this.net = net;
    }

    public Point getFarBottomNetEnd() {
        return net[1];
    }

    public Point getFarTopNetEnd() {
        return net[3];
    }

    public Point getCloseBottomNetEnd() {
        return net[0];
    }

    public Point getCloseTopNetEnd() {
        return net[2];
    }

    public Point[] getNet() {
        return net;
    }

    public Point getCornerDownLeft() {
        return corners[0];
    }

    public Point getCornerDownRight() {
        return corners[1];
    }

    public Point getCornerTopRight() {
        return corners[2];
    }

    public Point getCornerTopLeft() {
        return corners[3];
    }

    public Point[] getCorners() {
        return corners;
    }

    public static Table makeTableFromProperties(Properties properties) {
        int x;
        int y;
        Point[] corners = new Point[4];
        Point[] net = new Point[4];
        for (int i = 1; i<5; i++) {
            x = Integer.parseInt(properties.getProperty("n"+i+"_x"));
            y = Integer.parseInt(properties.getProperty("n"+i+"_y"));
            net[i-1] = new Point(x,y);
            x = Integer.parseInt(properties.getProperty("c"+i+"_x"));
            y = Integer.parseInt(properties.getProperty("c"+i+"_y"));
            corners[i-1] = new Point(x,y);
        }
        return new Table(corners, net);
    }

    public static Table makeTableFromIntArray(int[] cornerInts) {
        if (cornerInts.length < 8) throw new NotFourCornersException(cornerInts.length/2);
        Point[] points = intToPointArray(cornerInts);
        Point[] netPoints = new Point[2];
        netPoints[0] = calcNetPoint(points[0], points[1]);
        netPoints[1] = calcNetPoint(points[2], points[3]);
        return new Table(points, netPoints);
    }

    private static Point calcNetPoint(Point oneCorner, Point oppositeCornerHorizontally) {
        return new Point(Math.abs((oneCorner.x+oppositeCornerHorizontally.x)/2), Math.abs((oneCorner.y+oppositeCornerHorizontally.y)/2));
    }

    private static Point[] intToPointArray(int[] ints) {
        Point[] points = new Point[ints.length/2];
        for (int i = 0; i<points.length; i++) {
            Point point = new Point();
            point.x = ints[i*2];
            point.y = ints[i*2+1];
            points[i] = point;
        }
        return points;
    }

    public boolean isOn(int x) {
        double leftThreshold = this.getCornerTopLeft().x * 1.05;
        double rightThreshold = this.getCornerTopRight().x / 1.05;
        double bottomThreshold = this.getCornerDownLeft().y;
        return (detection.centerX >= leftThreshold && detection.centerX <= rightThreshold && detection.centerY <= bottomThreshold);
    }

    static class NotFourCornersException extends RuntimeException {
        private static final String MESSAGE = "Table needs 4 points as corners, you provided: ";
        NotFourCornersException(int amountOfCorners) {
            super(MESSAGE + amountOfCorners);
        }
    }

    static class NotFourNetCornersException extends RuntimeException {
        private static final String MESSAGE = "Net needs 4 points which mark the 4 corners of the net, you provided: ";
        NotFourNetCornersException(int amountOfPoints) {
            super(MESSAGE + amountOfPoints);
        }
    }
}
