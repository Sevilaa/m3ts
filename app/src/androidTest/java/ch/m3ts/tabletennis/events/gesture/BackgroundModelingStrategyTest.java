package ch.m3ts.tabletennis.events.gesture;

import android.app.Activity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.LinkedList;
import java.util.List;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import ch.m3ts.MainActivity;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BackgroundModelingStrategyTest {
    private BackgroundModelingStrategy gestureDetectionStrategy;
    private Mat background;
    private List<Mat> imgsWithRackets;
    private List<Mat> imgsWithNoRackets;

    @Rule
    public ActivityTestRule<MainActivity> pmsMainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        gestureDetectionStrategy = new BackgroundModelingStrategy();
        loadPicturesToMats();
    }

    @Test
    public void testIsRacketInPicture() {
        for (Mat img : imgsWithRackets) {
            initializeDetectorWithStillImages();
            assertTrue(gestureDetectionStrategy.isRacketInPicture(img));
        }

        for (Mat img : imgsWithNoRackets) {
            initializeDetectorWithStillImages();
            assertFalse(gestureDetectionStrategy.isRacketInPicture(img));
        }
    }

    private void loadPicturesToMats() {
        if (!OpenCVLoader.initDebug()) fail();
        imgsWithRackets = new LinkedList<>();
        imgsWithNoRackets = new LinkedList<>();
        String[] filesWithRacket = {
                "test_gesture_outdoor_black_racket.jpg",
                "test_gesture_outdoor_red_racket.jpg",
                "test_gesture_outdoor_dark_red_racket.jpg",
                "test_gesture_outdoor_light_red_racket.jpg",
                "test_gesture_outdoor_pink_racket.jpg",
        };
        String[] filesWithNoRacket = {
                "test_gesture_outdoor_black_pants.jpg",
        };

        Activity activity = pmsMainActivityRule.getActivity();
        for (String filename : filesWithRacket) {
            imgsWithRackets.add(TestHelper.readImageFromAssets(filename, activity));
        }
        for (String filename : filesWithNoRacket) {
            imgsWithNoRackets.add(TestHelper.readImageFromAssets(filename, activity));
        }
        background = TestHelper.readImageFromAssets("test_gesture_outdoor_nothing.jpg", activity);
    }

    private void initializeDetectorWithStillImages() {
        gestureDetectionStrategy = new BackgroundModelingStrategy();
        for (int i = 0; i < 5; i++) {
            Mat frame = new Mat();
            background.copyTo(frame);
            assertFalse(gestureDetectionStrategy.isRacketInPicture(frame));
        }
    }
}
