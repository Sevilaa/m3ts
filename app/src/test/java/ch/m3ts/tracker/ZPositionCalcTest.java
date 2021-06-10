package ch.m3ts.tracker;

import org.junit.Before;
import org.junit.Test;

import ch.m3ts.detection.ZPositionCalc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ZPositionCalcTest {
    private final int TABLE_LENGTH_PX = 1160;   // from 2d_6.xml
    private final int VIDEO_WIDTH_PX = 1280;    // from 2d_6.xml
    private final double HORIZONTAL_VIEW_ANGLE = 66.56780242919922;     // from mobile phone used for all recordings
    private final double BALL_RADIUS_FURTHEST_EDGE_PX = 5.09382053830555;   // calculated beforehand
    private final double BALL_RADIUS_CLOSEST_EDGE_PX = 8.467153284671532;   // calculated beforehand
    private final double ACCURACY_OF_CALCULATIONS_MM = 5;
    private final int MAX_OFFSET_MM = 100;  // from ZPositionCalc

    private ZPositionCalc calc;

    @Before
    public void setUp() {
        calc = new ZPositionCalc(HORIZONTAL_VIEW_ANGLE, TABLE_LENGTH_PX, VIDEO_WIDTH_PX);
    }

    @Test
    public void testTableDistance() {
        double[] tableDistance = calc.getTableDistanceMM();
        double[] expectedTableDistance = new double[]{tableDistance[0], tableDistance[0] + ZPositionCalc.TABLE_TENNIS_TABLE_WIDTH_MM};

        assertEquals(2, tableDistance.length);
        assertEquals(expectedTableDistance[1], tableDistance[1], 0);
    }

    @Test
    public void testBallRadiusToZPos() {
        double zPosMm = calc.findZPosOfBallMm(BALL_RADIUS_FURTHEST_EDGE_PX);
        assertEquals(ZPositionCalc.TABLE_TENNIS_TABLE_WIDTH_MM + ZPositionCalc.MAX_OFFSET_MM, zPosMm, ACCURACY_OF_CALCULATIONS_MM);
        zPosMm = calc.findZPosOfBallMm(BALL_RADIUS_CLOSEST_EDGE_PX);
        assertEquals(ZPositionCalc.MAX_OFFSET_MM, zPosMm, ACCURACY_OF_CALCULATIONS_MM);

        // going over the table
        zPosMm = calc.findZPosOfBallMm(BALL_RADIUS_CLOSEST_EDGE_PX + 0.03);
        assertTrue(zPosMm < ZPositionCalc.MAX_OFFSET_MM);
        zPosMm = calc.findZPosOfBallMm(BALL_RADIUS_FURTHEST_EDGE_PX - 0.03);
        assertTrue(zPosMm > ZPositionCalc.TABLE_TENNIS_TABLE_WIDTH_MM);

        // checking for min and max zpos
        zPosMm = calc.findZPosOfBallMm(BALL_RADIUS_CLOSEST_EDGE_PX * 300);
        assertEquals(0, zPosMm, 0);
        zPosMm = calc.findZPosOfBallMm(BALL_RADIUS_FURTHEST_EDGE_PX * 0.1);
        assertEquals(ZPositionCalc.TABLE_TENNIS_TABLE_WIDTH_MM + MAX_OFFSET_MM * 2, zPosMm, ACCURACY_OF_CALCULATIONS_MM);
    }
}