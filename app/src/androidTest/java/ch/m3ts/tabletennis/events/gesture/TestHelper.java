package ch.m3ts.tabletennis.events.gesture;

import android.app.Activity;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import ch.m3ts.util.Log;

import static junit.framework.Assert.fail;

public class TestHelper {
    public static Mat readImageFromAssets(String filename, Activity activity) {
        Mat mat = null;
        try (InputStream is = activity.getAssets().open(filename)) {
            File file = new File(activity.getCacheDir() + "/" + filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer);
            fos.close();
            if (file.exists()) {
                mat = Imgcodecs.imread(file.getAbsolutePath());
            } else {
                fail(String.format("could not read file from assets, is %s in Assets dir?", filename));
            }
        } catch (Exception e) {
            Log.d(e.getMessage());
            fail();
        }
        return mat;
    }
}
