//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.util;

import java.util.Random;

public class Base64 {
    public static final int CRLF = 4;
    public static final int DEFAULT = 0;
    public static final int NO_CLOSE = 16;
    public static final int NO_PADDING = 1;
    public static final int NO_WRAP = 2;
    public static final int URL_SAFE = 8;

    Base64() {
        throw new RuntimeException("Stub!");
    }

    public static byte[] decode(String str, int flags) {
        byte[] randomBytes = new byte[str.length()];
        new Random().nextBytes(randomBytes);
        return randomBytes;
    }

    public static byte[] decode(byte[] input, int flags) {
        throw new RuntimeException("Stub!");
    }

    public static byte[] decode(byte[] input, int offset, int len, int flags) {
        throw new RuntimeException("Stub!");
    }

    public static String encodeToString(byte[] input, int flags) {
        return "I'm an encoded byte array";
    }

    public static String encodeToString(byte[] input, int offset, int len, int flags) {
        throw new RuntimeException("Stub!");
    }

    public static byte[] encode(byte[] input, int flags) {
        throw new RuntimeException("Stub!");
    }

    public static byte[] encode(byte[] input, int offset, int len, int flags) {
        throw new RuntimeException("Stub!");
    }
}
