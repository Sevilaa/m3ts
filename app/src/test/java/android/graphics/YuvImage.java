package android.graphics;

import java.io.IOException;
import java.io.OutputStream;

public class YuvImage {
    private byte[] bytes;
    private int width;
    private int height;

    public YuvImage(byte[] yuv, int format, int width, int height, int[] strides) {
        this.bytes = yuv;
        this.width = width;
        this.height = height;
    }

    public boolean compressToJpeg(Rect rectangle, int quality, OutputStream stream) {
        try {
            stream.write(bytes);
        } catch (IOException ex) {
            // ignore in stub
            return false;
        }
        return true;
    }

    public byte[] getYuvData() {
        return bytes;
    }

    public int getYuvFormat() {
        return 17; // => NV21
    }

    public int[] getStrides() {
        return null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}