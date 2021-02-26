package ch.m3ts.tracker.init;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.opencv.android.OpenCVLoader;

import java.lang.ref.WeakReference;

import ch.m3ts.Log;
import ch.m3ts.pubnub.CameraBytesConversions;
import ch.m3ts.tracker.visualization.CameraPreviewActivity;
import cz.fmo.R;
import cz.fmo.camera.CameraThread;


/**
 * Handler of the InitTrackerActivity, does the processing work for the activity on a separate thread.
 */
public class InitTrackerHandler extends android.os.Handler implements CameraThread.Callback, InitTrackerCallback {
    private static final int CAMERA_ERROR = 2;
    private final WeakReference<InitTrackerActivity> mActivity;
    private int cameraHeight = 0;
    private int cameraWidth = 0;
    private byte[] currentFrame;
    private int[] tableCorners;
    private int selectedMatchType;
    private int selectedServingSide;
    private String selectedMatchId;
    private boolean isReadingQRCode = true;

    public InitTrackerHandler(@NonNull InitTrackerActivity activity) {
        mActivity = new WeakReference<>(activity);
        if(!OpenCVLoader.initDebug()) {
            Log.d("OpenCV not loaded!");
        } else {
            Log.d("OpenCV loaded!");
        }
    }

    @Override
    public void onCameraRender() {
        // no implementation
    }

    @Override
    public void onCameraFrame(byte[] dataYUV420SP) {
        this.currentFrame = dataYUV420SP;
        if(this.isReadingQRCode) {
            setCameraSize(this.mActivity.get());
            BinaryBitmap binaryBitmap = convertBytesToBinaryBitmap(dataYUV420SP);
            String result = readQRCode(binaryBitmap);
            parseQRCodeData(result);
        }
    }

    @Override
    public void onCameraError() {
        if (hasMessages(CAMERA_ERROR)) return;
        sendMessage(obtainMessage(CAMERA_ERROR));
    }

    @Override
    public byte[] onCaptureFrame() {
        final Activity activity = mActivity.get();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.findViewById(R.id.tracker_loading).setVisibility(View.VISIBLE);
            }
        });
        return this.currentFrame;
    }

    @Override
    public void switchToLiveActivity() {
        if(this.tableCorners != null) {
            mActivity.get().switchToLiveActivity(this.selectedMatchId, this.selectedMatchType, this.selectedServingSide, this.tableCorners);
        }
    }

    @Override
    public void setTableCorners(int[] tableCorners) {
        this.tableCorners = tableCorners;
    }

    public int getCameraHeight() {
        return this.cameraHeight;
    }

    public int getCameraWidth() {
        return this.cameraWidth;
    }

    @Override
    public void updateLoadingBar(int partNumber) {
        ProgressBar bar = mActivity.get().findViewById(R.id.loading_bar);
        bar.setProgress(partNumber);
    }

    @Override
    public void setLoadingBarSize(int size) {
        ProgressBar bar = mActivity.get().findViewById(R.id.loading_bar);
        bar.setMax(size);
        bar.setProgress(0);
    }

    @Override
    public void frameSent() {
        final Activity activity = mActivity.get();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.findViewById(R.id.loading_bar_background).setVisibility(View.GONE);
                ((TextView)activity.findViewById(R.id.tracker_info)).setText(activity.getResources().getString(R.string.tiWaitingText));
            }
        });
    }

    private void setCameraSize(CameraPreviewActivity activity) {
        this.cameraHeight = activity.getCameraHeight();
        this.cameraWidth = activity.getCameraWidth();
    }

    private BinaryBitmap convertBytesToBinaryBitmap(byte[] bytes) {
        Bitmap bitmap = CameraBytesConversions.compressAndConvertCameraImageBytes(bytes, this.getCameraWidth(), this.getCameraHeight());
        LuminanceSource source = new PlanarYUVLuminanceSource(bytes, bitmap.getWidth(), bitmap.getHeight(), 0, 0, bitmap.getWidth(), bitmap.getHeight(), false);
        return new BinaryBitmap(new HybridBinarizer(source));
    }

    private String readQRCode(BinaryBitmap binaryBitmap) {
        Reader reader = new QRCodeReader();
        String stringResult = "";
        try {
            Result result = reader.decode(binaryBitmap);
            stringResult = result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            Log.d("QR-Code not found");
        }
        return stringResult;
    }

    private void parseQRCodeData(String result) {
        if (result != null && !result.isEmpty()) {
            try {
                String[] resultArray = result.split(";");
                if (resultArray.length >= 3) {
                    this.selectedMatchId = resultArray[0];
                    this.selectedMatchType = Integer.parseInt(resultArray[1]);
                    this.selectedServingSide = Integer.parseInt(resultArray[2]);
                    mActivity.get().enterPubNubRoom(this.selectedMatchId);
                    this.isReadingQRCode = false;
                    hideScanOverlay();
                }
            } catch (Exception e) {
                Log.d("Data of QR-Code is incorrect");
                Log.d(e.getMessage());
            }
        }
    }

    private void hideScanOverlay() {
        final Activity activity = mActivity.get();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.findViewById(R.id.scan_overlay).setVisibility(View.GONE);
            }
        });
    }
}
