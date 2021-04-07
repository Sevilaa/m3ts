package ch.m3ts.helper;

import android.os.Environment;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

import ch.m3ts.Log;

public class OpenCVHelper {
    private OpenCVHelper() {
    }

    public static void saveImage(Mat mat, String fileName) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String fileWithEnding = fileName + ".png";
        File file = new File(path, fileWithEnding);

        Boolean bool = null;
        fileWithEnding = file.toString();
        bool = Imgcodecs.imwrite(fileWithEnding, mat);

        if (bool)
            Log.d("SUCCESS writing image to storage");
        else
            Log.d("Fail writing image to storage");
    }
}
