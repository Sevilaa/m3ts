package cz.fmo.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Provides access to files located in the directory writable by the application.
 */
public final class FileManager {
    private final Context mContext;
    private String testSet;
    private static final String M3TS_DIR = "/M3TS";
    private static final String BENCHMARK_DIR = "/benchmark/";

    /**
     * @param context for activities, you should generally pass "this" here
     */
    public FileManager(Context context) {
        mContext = context;
    }

    public FileManager(Context context, String testSet) {
        mContext = context;
        this.testSet = testSet;
    }

    private File publicDir() {
        String state = Environment.getExternalStorageState();

        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return fallbackPublicDir();
        }
        String videoPaths = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + M3TS_DIR;
        if (testSet != null) videoPaths += BENCHMARK_DIR + testSet;
        File path = new File(videoPaths);
        if (!path.exists()) {
            if (!path.mkdirs()) {
                return fallbackPublicDir();
            }
        }

        return path;
    }

    private File fallbackPublicDir() {
        return privateDir();
    }

    private File privateDir() {
        return mContext.getFilesDir();
    }

    /**
     * @return File object representing a new or existing file in the public storage directory.
     */
    public File open(String name) {
        return new File(publicDir(), name);
    }

    /**
     * @return File object representing a new or existing file in the private storage directory.
     */
    public File privateOpen(String name) {
        return new File(privateDir(), name);
    }

    /**
     * Updates the system catalog so that the new media file shows in compatible apps.
     */
    public void newMedia(File file) {
        MediaScannerConnection.scanFile(mContext, new String[]{file.getAbsolutePath()}, null, null);
    }

    public String[] listMP4() {
        final Pattern p = Pattern.compile(".*\\.mp4");
        String[] out = publicDir().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return p.matcher(name).matches();
            }
        });
        if(out == null) {
            out = new String[0];
        }
        Arrays.sort(out);
        return out;
    }
}
