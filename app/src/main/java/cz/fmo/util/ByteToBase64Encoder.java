package cz.fmo.util;

import android.util.Base64;

public class ByteToBase64Encoder {
    public static String encodeToString(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] decodeToByte(String bytesString) {
        return Base64.decode(bytesString, Base64.DEFAULT);
    }
}
