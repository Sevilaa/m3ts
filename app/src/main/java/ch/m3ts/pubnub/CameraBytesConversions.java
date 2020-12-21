package ch.m3ts.pubnub;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

/**
 * Class which formats bytes received from the Camera API to a Bitmap or RGB Bytes.
 */
public class CameraBytesConversions {
    private static final int CAMERA_IMAGE_FORMAT = ImageFormat.NV21;
    private static final int PICTURE_QUALITY = 70;

    private CameraBytesConversions(){}

    /**
     * Compresses and converts image bytes received from the Camera API to a Bitmap.
     * @param yuvBytes - image bytes
     * @param width - image width
     * @param height - image height
     * @return Bitmap
     */
    public static Bitmap compressAndConvertCameraImageBytes(byte[] yuvBytes, int width, int height) {
        byte[] compressedJPGBytes = compressCameraImageBytes(yuvBytes, width, height);
        return BitmapFactory.decodeByteArray(compressedJPGBytes, 0, compressedJPGBytes.length);
    }

    /**
     * Compresses image bytes (NV21) received from the Camera API.
     * @param yuvBytes - image bytes
     * @param width - image width
     * @param height - image height
     * @return compressed JPG byte[]
     */
    public static byte[] compressCameraImageBytes(byte[] yuvBytes, int width, int height) {
        YuvImage image = new YuvImage(yuvBytes, CAMERA_IMAGE_FORMAT, width, height, null);
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0,0, width, height), PICTURE_QUALITY, byteArrayOut);
        return byteArrayOut.toByteArray();
    }
}