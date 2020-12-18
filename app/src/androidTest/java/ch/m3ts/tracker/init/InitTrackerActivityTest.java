package ch.m3ts.tracker.init;

import android.content.Intent;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import ch.m3ts.tracker.visualization.CameraPreviewActivity;
import cz.fmo.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class InitTrackerActivityTest extends InstrumentationTestCase {
    private InitTrackerActivity activity;
    private String QR_CODE_PATH = "test_qr_code.yuv";
    private String SCAN_OVERLAY_TEXT = "Scan the QR code from the display";
    private String WAIT_FOR_PICTURE_TEXT = "Sending Frame...";

    @Rule
    public ActivityTestRule<InitTrackerActivity> initActivityRule = new ActivityTestRule<InitTrackerActivity>(InitTrackerActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            return new Intent(Intent.ACTION_MAIN);
        }
    };

    @Before
    public void setUp() {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        activity = initActivityRule.getActivity();
    }

    @After
    public void tearDown() {
        activity.finish();
        activity = null;
    }

    @Test
    public void scanOverlayDisplayed() {
        onView(withId(R.id.scan_overlay)).check(matches(isDisplayed()));
        onView(withText(SCAN_OVERLAY_TEXT)).check(matches(isDisplayed()));
    }

    @Test
    public void waitForPictureLayoutDisplayed() throws IllegalAccessException, NoSuchFieldException, IOException {
        Field cameraCallbackField = CameraPreviewActivity.class.getDeclaredField("cameraCallback");
        cameraCallbackField.setAccessible(true);
        InitTrackerHandler cameraCallback = (InitTrackerHandler) cameraCallbackField.get(activity);
        cameraCallback.onCameraFrame(loadQRCodeBytes());
        onView(withId(R.id.scan_overlay)).check(doesNotExist());
        //onView(withId(R.id.tracker_loading)).check(matches(isDisplayed()));
        //onView(withText(WAIT_FOR_PICTURE_TEXT)).check(matches(isDisplayed()));
    }

    private byte[] loadQRCodeBytes() throws IOException {
        InputStream inputStream = activity.getAssets().open(QR_CODE_PATH);
        byte[] fileBytes = new byte[inputStream.available()];
        inputStream.read(fileBytes);
        inputStream.close();
        return fileBytes;
    }
}
