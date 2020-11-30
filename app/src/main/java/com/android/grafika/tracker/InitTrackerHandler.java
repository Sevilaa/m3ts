package com.android.grafika.tracker;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.android.grafika.CameraPreviewActivity;
import com.android.grafika.Log;
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

import java.lang.ref.WeakReference;
import java.nio.IntBuffer;

import cz.fmo.camera.CameraThread;
import helper.ColorConversions;

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

    public InitTrackerHandler(@NonNull InitTrackerActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void onCameraRender() {
        // no implementation
    }

    @Override
    public void onCameraFrame(byte[] dataYUV420SP) {
        this.currentFrame = dataYUV420SP;
        setCameraSize(this.mActivity.get());
        BinaryBitmap binaryBitmap = convertBytesToBinaryBitmap(dataYUV420SP);
        String result = readQRCode(binaryBitmap);
        parseQRCodeData(result);
    }

    @Override
    public void onCameraError() {
        if (hasMessages(CAMERA_ERROR)) return;
        sendMessage(obtainMessage(CAMERA_ERROR));
    }

    @Override
    public byte[] onCaptureFrame() {
        return this.currentFrame;
    }

    @Override
    public void switchToDebugActivity() {
        mActivity.get().switchToDebugActivity(this.selectedMatchId, this.selectedMatchType, this.selectedServingSide, this.tableCorners);
    }

    @Override
    public void setTableCorners(int[] tableCorners) {
        this.tableCorners = tableCorners;
    }

    private BinaryBitmap convertBytesToBinaryBitmap(byte[] bytes) {
        int[] out = new int[this.cameraHeight*this.cameraWidth];
        ColorConversions.yuv420pToRGBA8888(out, bytes, this.cameraWidth, this.cameraHeight);
        Bitmap bitmap = Bitmap.createBitmap(this.cameraWidth, this.cameraHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(out));
        LuminanceSource source = new PlanarYUVLuminanceSource(bytes, bitmap.getWidth(), bitmap.getHeight(), 0, 0, bitmap.getWidth(), bitmap.getHeight(), false);
        return new BinaryBitmap(new HybridBinarizer(source));
    }

    private void setCameraSize(CameraPreviewActivity activity) {
        this.cameraHeight = activity.getCameraHeight();
        this.cameraWidth = activity.getCameraWidth();
    }

    private String readQRCode(BinaryBitmap binaryBitmap) {
        Reader reader = new QRCodeReader();
        String stringResult = "";
        try {
            Result result = reader.decode(binaryBitmap);
            stringResult = result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            Log.d("QR-Code not found");
            e.printStackTrace();
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
                    mActivity.get().createPubNubRoom(this.selectedMatchId);
                }
            } catch (Exception e) {
                Log.d("Data of QR-Code is incorrect");
                e.printStackTrace();
            }
        }
    }
}
