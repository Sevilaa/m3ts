package ch.m3ts.connection.pubnub;

import android.util.Base64;

/**
 * Small Wrapper class to ensure base64 encoding and decoding is done via the same flag.
 */
public class ByteToBase64 {
    private static final int FLAG = Base64.DEFAULT;

    private ByteToBase64() {
    }

    public static String encodeToString(byte[] bytes) {
        return Base64.encodeToString(bytes, FLAG);
    }

    public static byte[] decodeToByte(String bytesString) {
        return Base64.decode(bytesString, FLAG);
    }
}
