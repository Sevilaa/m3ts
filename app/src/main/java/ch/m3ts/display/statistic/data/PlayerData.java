package ch.m3ts.display.statistic.data;

import java.io.Serializable;

import ch.m3ts.util.Side;

public class PlayerData implements Serializable {
    private final Side side;
    private final int strikes;
    private final float fastestStrike;
    private int points;

    public PlayerData(Side side, int strikes, float fastestStrike, int points) {
        this.side = side;
        this.strikes = strikes;
        this.fastestStrike = fastestStrike;
        this.points = points;
    }

    public Side getSide() {
        return side;
    }

    public int getStrikes() {
        return strikes;
    }

    public float getFastestStrike() {
        return fastestStrike;
    }

    public int getPoints() {
        return points;
    }
}
