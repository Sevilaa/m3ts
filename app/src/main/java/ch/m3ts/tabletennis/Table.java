package ch.m3ts.tabletennis;

import android.graphics.Point;
import android.support.annotation.NonNull;

import java.util.Properties;

import ch.m3ts.tabletennis.helper.Side;

/**
 * Representation of a table tennis table.
 * Contains arrays to pinpoint the location of each table corner as well as the net.
 * <p>
 * Can be created either by an int[] containing four table corners or by an XML file inside the
 * assets folder.
 */
public class Table {
    private static final double NET_HEIGHT_TO_LENGTH_RATIO = 0.055;
    private final Point[] corners;
    private final Point net;
    private final Point netTop;
    private final int tableLength;

    public Table(@NonNull Point[] corners, @NonNull Point net) {
        if (corners.length != 2) {
            throw new NotTwoCornersException(corners.length);
        }
        this.corners = corners;
        this.net = net;
        this.tableLength = getCornerDownRight().x - getCornerDownLeft().x;
        this.netTop = new Point(this.net.x, (int) Math.round(net.y - tableLength * NET_HEIGHT_TO_LENGTH_RATIO));
    }

    public static Table makeTableFromProperties(Properties properties) {
        int x;
        int y;
        Point net;
        x = Integer.parseInt(properties.getProperty("n1_x"));
        y = Integer.parseInt(properties.getProperty("n1_y"));
        net = new Point(x, y);
        Point[] corners = new Point[2];
        for (int i = 1; i < 3; i++) {
            x = Integer.parseInt(properties.getProperty("c" + i + "_x"));
            y = Integer.parseInt(properties.getProperty("c" + i + "_y"));
            corners[i - 1] = new Point(x, y);
        }
        return new Table(corners, net);
    }

    public static Table makeTableFromIntArray(int[] cornerInts) {
        if (cornerInts.length < 4) throw new NotTwoCornersException(cornerInts.length / 2);
        Point[] points = intToPointArray(cornerInts);
        Point net = calcNetPoint(points[0], points[1]);
        return new Table(points, net);
    }

    private static Point calcNetPoint(Point oneCorner, Point oppositeCornerHorizontally) {
        return new Point(Math.abs((oneCorner.x + oppositeCornerHorizontally.x) / 2), Math.abs((oneCorner.y + oppositeCornerHorizontally.y) / 2));
    }

    private static Point[] intToPointArray(int[] ints) {
        Point[] points = new Point[ints.length / 2];
        for (int i = 0; i < points.length; i++) {
            Point point = new Point();
            point.x = ints[i * 2];
            point.y = ints[i * 2 + 1];
            points[i] = point;
        }
        return points;
    }

    public Point getNetBottom() {
        return net;
    }

    public Point getNetTop() {
        return netTop;
    }

    public Point getCornerDownLeft() {
        return corners[0];
    }

    public Point getCornerDownRight() {
        return corners[1];
    }

    public Point[] getCorners() {
        return corners;
    }

    public boolean isOnOrAbove(int x, int y) {
        double leftThreshold = this.getCornerDownLeft().x;
        double rightThreshold = this.getCornerDownRight().x;
        double bottomThreshold = this.getNetBottom().y;
        return (x >= leftThreshold && x <= rightThreshold && y <= bottomThreshold);
    }

    public boolean isBounceOn(int x, int y) {
        double leftThreshold = this.getCornerDownLeft().x;
        double rightThreshold = this.getCornerDownRight().x;
        double bottomThreshold = this.getNetBottom().y;
        double topThreshold = this.net.y * 0.85;    // 0.85 evaluated by play testing
        return (x >= leftThreshold && x <= rightThreshold && y <= bottomThreshold && y >= topThreshold);
    }

    public boolean isBelow(int x, int y) {
        double leftThreshold = this.getCornerDownLeft().x;
        double rightThreshold = this.getCornerDownRight().x;
        double bottomThreshold = this.getNetBottom().y * 1.05;
        return (x >= leftThreshold && x <= rightThreshold && y > bottomThreshold);
    }

    public int getWidth() {
        return tableLength;
    }

    public Side getHorizontalSideOfDetection(int centerX) {
        Side horizontalSide = null;
        if (centerX < this.getNetBottom().x * 0.95) horizontalSide = Side.LEFT;
        else if (centerX > this.getNetBottom().x * 1.05) horizontalSide = Side.RIGHT;
        return horizontalSide;
    }

    public static class NotTwoCornersException extends RuntimeException {

        private static final String MESSAGE = "Table needs 2 points as corners, you provided: ";

        NotTwoCornersException(int amountOfCorners) {
            super(MESSAGE + amountOfCorners);
        }
    }
}
