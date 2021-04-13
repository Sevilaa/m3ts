package ch.m3ts.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("SameParameterValue")
public class Log {
    private static final String TAG = "M3TS";

    private Log() {
    }

    public static void d(String msg) {
        android.util.Log.d(TAG, msg);
    }

    public static void d(String msg, File file) {
        try (BufferedWriter sw = new BufferedWriter(new FileWriter(file, true))) {
            msg = msg + "\n";
            sw.append(msg);
        } catch (IOException ex) {
            Log.e(ex.getMessage(), ex);
        }
    }

    public static void e(String msg, Exception e) {
        android.util.Log.e(TAG, msg, e);
    }

    public static void i(String msg) {
        android.util.Log.i(TAG, msg);
    }

    public static void w(String msg) {
        android.util.Log.w(TAG, msg);
    }
}
