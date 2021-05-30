package ch.m3ts.tabletennis.events;

import android.graphics.Point;

import org.junit.Before;
import org.junit.Test;

import ch.m3ts.detection.BallCurvePredictor;
import ch.m3ts.detection.LinearBallCurvePredictor;
import ch.m3ts.util.Table;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LinearBallCurvePredictorTest {
    private BallCurvePredictor curvePredictor;
    private Table table;

    @Before
    public void init() {
        curvePredictor = new LinearBallCurvePredictor();
        table = mock(Table.class);
        when(table.getNetBottom()).thenReturn(new Point(50, 50));
        when(table.getNetTop()).thenReturn(new Point(50, 10));
    }

    @Test
    public void testMovementsWhichGoIntoNet() {
        int[] cxGoingIntoNet = {
                20, 30, 40
        };
        int[] cyGoingIntoNet = {
                10, 15, 20
        };

        assertTrue(curvePredictor.willBallMoveIntoNet(cxGoingIntoNet, cyGoingIntoNet, table));
    }

    @Test
    public void testMovementsWhichDoNotGoIntoNet() {
        int[] cxNotGoingIntoNet = {
                20, 30, 40
        };
        // overshoot
        int[] cyNotGoingIntoNet = {
                50, 30, 15
        };
        assertFalse(curvePredictor.willBallMoveIntoNet(cxNotGoingIntoNet, cyNotGoingIntoNet, table));

        // undershoot
        cyNotGoingIntoNet = new int[]{
                20, 40, 55
        };
        assertFalse(curvePredictor.willBallMoveIntoNet(cxNotGoingIntoNet, cyNotGoingIntoNet, table));

    }
}