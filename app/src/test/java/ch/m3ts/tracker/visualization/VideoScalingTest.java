package ch.m3ts.tracker.visualization;

import android.graphics.Point;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VideoScalingTest {
    private VideoScaling videoScaling;
    private int videoWidth;
    private int videoHeight;
    private int canvasWidth;
    private int canvasHeight;

    @Before
    public void setUp() {
        this.videoWidth = 200;
        this.videoHeight = 100;
        this.canvasWidth = 200;
        this.canvasHeight = 100;
        this.videoScaling = new VideoScaling(this.videoWidth, this.videoHeight);
        videoScaling.setCanvasWidth(this.canvasWidth);
        videoScaling.setCanvasHeight(this.canvasHeight);
    }

    @After
    public void tearDown() {
        this.videoScaling = null;
    }

    @Test
    public void testGetVideoWidth() {
        assertEquals(videoScaling.getVideoWidth(), videoWidth);
    }

    @Test
    public void testGetVideoHeight() {
        assertEquals(videoScaling.getVideoHeight(), videoHeight);
    }

    @Test
    public void testGetCanvasWidth() {
        assertEquals(videoScaling.getCanvasWidth(), canvasWidth);
    }

    @Test
    public void testGetCanvasHeight() {
        assertEquals(videoScaling.getVideoHeight(), canvasHeight);
    }

    @Test
    public void testSetCanvasWidth() {
        this.canvasWidth = 500;
        videoScaling.setCanvasWidth(canvasWidth);
        assertEquals(videoScaling.getCanvasWidth(), canvasWidth);
    }

    @Test
    public void testSetCanvasHeight() {
        this.canvasHeight = 400;
        videoScaling.setCanvasHeight(canvasHeight);
        assertEquals(videoScaling.getCanvasHeight(), canvasHeight);
    }

    @Test
    public void testScaleY() {
        float testValue = 2000;
        int expectedScaledValue = Math.round(testValue / ((float) this.videoHeight ) * this.canvasHeight);
        int calculatedScaledValue = videoScaling.scaleY(testValue);
        assertEquals(calculatedScaledValue, expectedScaledValue);
    }

    @Test
    public void testScaleX() {
        int testValue = 1000;
        int expectedScaledValue = Math.round(((float) testValue) / ((float) this.videoWidth ) * this.canvasWidth);
        int calculatedScaledValue = videoScaling.scaleY(testValue);
        assertEquals(calculatedScaledValue, expectedScaledValue);
    }

    @Test
    public void testScalePoint() {
        Point testPoint = new Point(100, 200);
        int expectedScaledXValue = Math.round(testPoint.x / ((float) this.videoWidth ) * this.canvasWidth);
        int expectedScaledYValue = Math.round(testPoint.y / ((float) this.videoHeight ) * this.canvasHeight);
        Point calculatedScaledPoint = videoScaling.scalePoint(testPoint);
        assertEquals(calculatedScaledPoint.x, expectedScaledXValue);
        assertEquals(calculatedScaledPoint.y, expectedScaledYValue);
    }
}