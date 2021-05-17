package ch.m3ts.tracker.visualization;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import ch.m3ts.display.stats.DetectionData;
import cz.fmo.Lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ZPosVisualizerTest {
    private float originX;
    private float originY;
    private Canvas canvas;
    private ZPosVisualizer zPosVisualizer;
    private ArgumentCaptor<Float> xCapture;
    private ArgumentCaptor<Float> yCapture;
    private ArgumentCaptor<Float> xLengthCapture;
    private ArgumentCaptor<Float> yLengthCapture;


    @Before
    public void init() {
        Paint tablePaint = Mockito.mock(Paint.class);
        Paint detectionPaint = Mockito.mock(Paint.class);
        canvas = Mockito.mock(Canvas.class);
        originX = 100;      // some random values
        originY = 200;      // some random values
        zPosVisualizer = new ZPosVisualizer(detectionPaint, tablePaint, originX, originY);
        xCapture = ArgumentCaptor.forClass(Float.class);
        yCapture = ArgumentCaptor.forClass(Float.class);
        xLengthCapture = ArgumentCaptor.forClass(Float.class);
        yLengthCapture = ArgumentCaptor.forClass(Float.class);
    }

    @Test
    public void drawTableBirdView() {
        zPosVisualizer.drawTableBirdView(canvas);

        // it should draw 6 lines in total
        Mockito.verify(canvas, times(6)).drawLine(xCapture.capture(), yCapture.capture(), xLengthCapture.capture(), yLengthCapture.capture(), (Paint) any());
        List<Float> xValues = xCapture.getAllValues();
        List<Float> yValues = yCapture.getAllValues();
        List<Float> xLengthValues = xLengthCapture.getAllValues();
        List<Float> yLengthValues = yLengthCapture.getAllValues();

        // 3 lines from origin x position to other side of table (horizontals)
        assertEquals(originX, xValues.get(0), 0);
        assertEquals(originX + ZPosVisualizer.DEFAULT_WIDTH_PX, xLengthValues.get(0), 0);
        assertEquals(originX, xValues.get(2), 0);
        assertEquals(originX + ZPosVisualizer.DEFAULT_WIDTH_PX, xLengthValues.get(2), 0);
        assertEquals(originX, xValues.get(5), 0);
        assertEquals(originX + ZPosVisualizer.DEFAULT_WIDTH_PX, xLengthValues.get(5), 0);

        // assert that there's an offset drawn
        for (Float f : yValues) {
            assertNotEquals(originY, f);
        }

        // 3 vertical lines
        float diff = yLengthValues.get(1) - yValues.get(1);
        assertTrue(diff > 0);
        assertEquals(diff, yLengthValues.get(3) - yValues.get(3), 0);
        assertEquals(diff, yLengthValues.get(4) - yValues.get(4), 0);
    }

    @Test
    public void drawZPos() {
        int tableCornerLeftX = 10;
        int tableCornerRightX = 100;
        Lib.Detection[] detectionsNotToDrawOnTable = generateDetectionsNotToDrawOnTable(tableCornerLeftX, tableCornerRightX);
        for (Lib.Detection d : detectionsNotToDrawOnTable) {
            zPosVisualizer.drawZPos(canvas, d, tableCornerLeftX, tableCornerRightX);
        }
        verify(canvas, never()).drawCircle(anyFloat(), anyFloat(), anyFloat(), (Paint) any());
        Lib.Detection[] detectionsToDrawOnTable = generateDetectionsToDrawOnTable(tableCornerLeftX, tableCornerRightX);

        for (Lib.Detection d : detectionsToDrawOnTable) {
            zPosVisualizer.drawZPos(canvas, d, tableCornerLeftX, tableCornerRightX);
        }
        verify(canvas, times(detectionsToDrawOnTable.length)).drawCircle(anyFloat(), anyFloat(), anyFloat(), (Paint) any());
    }

    @Test
    public void drawZPosOverloaded() {
        int tableCornerLeftX = 10;
        int tableCornerRightX = 100;
        zPosVisualizer = Mockito.spy(zPosVisualizer);
        DetectionData detectionData = new DetectionData(15, 20, .8, 66, false, 1);
        zPosVisualizer.drawZPos(canvas, detectionData, tableCornerLeftX, tableCornerRightX);

        // verify that the same drawZPos method gets called when using detectionData
        verify(zPosVisualizer, times(1)).drawZPos((Canvas) any(), (Lib.Detection) any(), anyInt(), anyInt());
        verify(canvas, times(1)).drawCircle(anyFloat(), anyFloat(), anyFloat(), (Paint) any());
    }

    private Lib.Detection[] generateDetectionsToDrawOnTable(int leftCornerX, int rightCornerX) {
        Lib.Detection d1 = new Lib.Detection();
        d1.centerX = leftCornerX + 1;
        d1.centerZ = .5;
        Lib.Detection d2 = new Lib.Detection();
        d2.centerX = rightCornerX - 1;
        d2.centerZ = .9;
        Lib.Detection d3 = new Lib.Detection();
        d3.centerX = (int) Math.round(rightCornerX * .5);
        d3.centerZ = .01;
        Lib.Detection d4 = new Lib.Detection();
        d4.centerX = rightCornerX;
        d4.centerZ = 1;
        Lib.Detection d5 = new Lib.Detection();
        d5.centerX = leftCornerX;
        d5.centerZ = 0;

        return new Lib.Detection[]{
                d1, d2, d3, d4, d5
        };
    }

    private Lib.Detection[] generateDetectionsNotToDrawOnTable(int leftCornerX, int rightCornerX) {
        Lib.Detection d1 = new Lib.Detection();
        Lib.Detection d2 = new Lib.Detection();
        d2.centerX = leftCornerX - 1;
        d2.centerZ = .9;
        Lib.Detection d3 = new Lib.Detection();
        d3.centerX = rightCornerX + 1;
        d3.centerZ = .01;
        Lib.Detection d4 = new Lib.Detection();
        d4.centerX = rightCornerX;
        d4.centerZ = 1.1;
        Lib.Detection d5 = new Lib.Detection();
        d5.centerX = leftCornerX;
        d5.centerZ = -0.1;

        return new Lib.Detection[]{
                d1, d2, d3, d4, d5
        };
    }
}