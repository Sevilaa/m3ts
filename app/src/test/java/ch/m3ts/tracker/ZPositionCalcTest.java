package ch.m3ts.tracker;

import org.junit.Before;
import org.junit.Test;

import ch.m3ts.Log;

import static org.junit.Assert.*;

public class ZPositionCalcTest {
    private final double TABLE_TENNIS_TABLE_LENGTH_MM = 2740; // normed, see https://en.wikipedia.org/wiki/Table_tennis
    private final double TABLE_TENNIS_TABLE_WIDTH_MM = 1525; // normed, see https://en.wikipedia.org/wiki/Table_tennis
    private final int TABLE_LENGTH_PX = 1160;   // from 2d_6.xml
    private final int VIDEO_WIDTH_PX = 1280;    // from 2d_6.xml
    private final double HORIZONTAL_VIEW_ANGLE = 66.56780242919922;     // from mobile phone used for all recordings
    private final double BALL_RADIUS_FURTHEST_EDGE_PX = 5.09382053830555;   // calculated beforehand
    private final double BALL_RADIUS_CLOSEST_EDGE_PX = 8.467153284671532;   // calculated beforehand
    private final double ACCURACY_OF_CALCULATIONS = 1;
    private final int MAX_OFFSET_MM = 100;  // from ZPositionCalc

    private ZPositionCalc calc;

    @Before
    public void setUp() {
        calc = new ZPositionCalc(HORIZONTAL_VIEW_ANGLE, TABLE_LENGTH_PX, VIDEO_WIDTH_PX);
    }

    @Test
    public void testTableDistance() {
        double[] tableDistance = calc.getTableDistanceMM();
        double[] expectedTableDistance = new double[]{tableDistance[0], tableDistance[0] + TABLE_TENNIS_TABLE_WIDTH_MM};

        assertEquals(2, tableDistance.length);
        assertEquals(expectedTableDistance[1], tableDistance[1], 0);
    }

    @Test
    public void testBallRadiusToZPos() {
        double zPosMm = calc.findZPosMmOfBall(BALL_RADIUS_FURTHEST_EDGE_PX);
        assertEquals(TABLE_TENNIS_TABLE_WIDTH_MM, zPosMm, 1);
        zPosMm = calc.findZPosMmOfBall(BALL_RADIUS_CLOSEST_EDGE_PX);
        assertEquals(0, zPosMm, 1);

        // going over the table
        zPosMm = calc.findZPosMmOfBall(BALL_RADIUS_CLOSEST_EDGE_PX+0.03);
        assertTrue(zPosMm < 0);
        zPosMm = calc.findZPosMmOfBall(BALL_RADIUS_FURTHEST_EDGE_PX - 0.03);
        assertTrue(zPosMm > TABLE_TENNIS_TABLE_WIDTH_MM);

        // checking for min and max zpos
        zPosMm = calc.findZPosMmOfBall(BALL_RADIUS_CLOSEST_EDGE_PX*300);
        assertEquals(-MAX_OFFSET_MM, zPosMm, 1);
        zPosMm = calc.findZPosMmOfBall(BALL_RADIUS_FURTHEST_EDGE_PX*0.1);
        assertEquals(TABLE_TENNIS_TABLE_WIDTH_MM+MAX_OFFSET_MM, zPosMm, 1);
    }
}