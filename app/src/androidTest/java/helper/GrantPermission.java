package helper;

import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.fail;

public class GrantPermission {

    public static void grantAllPermissions() {
        // Stupid ass workaround because GrantPermissionRule won't work with Espresso for whatever reason
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        try {
            UiObject allowButton = device.findObject(new UiSelector().text("ALLOW"));
            if (allowButton.exists()) {
                allowButton.click();
                allowButton = device.findObject(new UiSelector().text("ALLOW"));
                allowButton.click();
            }
        } catch (UiObjectNotFoundException ex) {
            fail();
        }
    }
}
