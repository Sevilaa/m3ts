package ch.m3ts.helper;

import android.os.Environment;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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

    public static Mat convertYUVBytesToBGRMat(byte[] yuvBytes, int cameraWidth, int cameraHeight) {
        Mat yuv = new Mat(calculateYUVMatHeight(cameraHeight), cameraWidth, CvType.CV_8UC1);
        yuv.put(0, 0, yuvBytes);
        Mat bgr = new Mat();
        Imgproc.cvtColor(yuv, bgr, Imgproc.COLOR_YUV2BGR_NV21, 3);
        return bgr;
    }

    private static int calculateYUVMatHeight(int cameraHeight) {
        return cameraHeight + cameraHeight / 2;
    }
}
