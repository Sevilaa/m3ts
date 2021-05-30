package ch.m3ts.util;

public enum Side {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    public static Side getOpposite(Side side) {
        Side opposite = null;
        if (side == RIGHT) {
            opposite = LEFT;
        } else if (side == LEFT) {
            opposite = RIGHT;
        } else if (side == TOP) {
            opposite = BOTTOM;
        } else if (side == BOTTOM) {
            opposite = TOP;
        }
        return opposite;
    }

    public static Side getOppositeX(float directionX) {
        Side opposite = null;
        if (directionX == DirectionX.RIGHT) {
            opposite = LEFT;
        } else if (directionX == DirectionX.LEFT) {
            opposite = RIGHT;
        }
        return opposite;
    }
}
