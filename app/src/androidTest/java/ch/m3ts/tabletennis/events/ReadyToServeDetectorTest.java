package ch.m3ts.tabletennis.events;

import android.graphics.Point;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import ch.m3ts.MainActivity;
import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.helper.Side;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ReadyToServeDetectorTest {
    private Table table;
    private static final int CAMERA_WIDTH = 1920;
    private static final int CAMERA_HEIGHT = 1080;
    private ReadyToServeCallback callback;
    private ReadyToServeDetector detector;

    @Rule
    public ActivityTestRule<MainActivity> pmsMainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        OpenCVLoader.initDebug();
        table = Mockito.spy(new Table(new Point[]{new Point(100, 500), new Point(1000, 500)}, new Point(550, 500)));
        callback = Mockito.mock(ReadyToServeCallback.class);
    }

    @Test
    public void skipTwoOfThreeFrames() {
        detector = new ReadyToServeDetector(table, Side.RIGHT, callback, false);
        Mat redMat = getRedMat();
        detector.isReadyToServe(redMat);
        Mockito.verify(table, Mockito.times(0)).getWidth();
        detector.isReadyToServe(redMat);
        Mockito.verify(table, Mockito.times(0)).getWidth();
        detector.isReadyToServe(redMat);
        Mockito.verify(table, Mockito.times(1)).getWidth();
    }

    @Test
    public void holdRedSideFor15Frames() {
        detector = new ReadyToServeDetector(table, Side.RIGHT, callback, false);
        boolean result;
        for (int i = 0; i < 14; i++) {
            result = detector.isReadyToServe(getRedMat());
            assertFalse(result);
        }
        result = detector.isReadyToServe(getRedMat());
        assertTrue(result);
    }

    @Test
    public void holdBlackSideFor15FramesWithUseBlackSideActive() {
        detector = new ReadyToServeDetector(table, Side.RIGHT, callback, true);
        boolean result;
        for (int i = 0; i < 14; i++) {
            result = detector.isReadyToServe(getBlackMat());
            assertFalse(result);
        }
        result = detector.isReadyToServe(getBlackMat());
        assertTrue(result);
    }

    @Test
    public void holdBlackSideFor15FramesWithUseBlackSideInActive() {
        detector = new ReadyToServeDetector(table, Side.RIGHT, callback, false);
        boolean result;
        for (int i = 0; i < 15; i++) {
            result = detector.isReadyToServe(getBlackMat());
            assertFalse(result);
        }
    }

    @Test
    public void invalidColorAfter14Frames() {
        detector = new ReadyToServeDetector(table, Side.RIGHT, callback, false);
        boolean result;
        for (int i = 0; i < 15; i++) {
            Mat mat = getBlackMat();
            if (i == 14) {
                mat = getGreenMat();
            }
            result = detector.isReadyToServe(mat);
            assertFalse(result);
        }

    }

    private Mat getRedMat() {
        return new Mat(CAMERA_WIDTH, CAMERA_HEIGHT, CvType.CV_8UC3, new Scalar(0, 0, 255));
    }

    private Mat getBlackMat() {
        return new Mat(CAMERA_WIDTH, CAMERA_HEIGHT, CvType.CV_8UC3, new Scalar(0, 0, 0));
    }

    private Mat getGreenMat() {
        return new Mat(CAMERA_WIDTH, CAMERA_HEIGHT, CvType.CV_8UC3, new Scalar(0, 255, 0));
    }
}