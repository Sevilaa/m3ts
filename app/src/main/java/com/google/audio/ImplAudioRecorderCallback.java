package com.google.audio;

import android.os.Handler;
import android.os.Looper;

import com.google.audio.calculators.AudioCalculator;

/**
 * Callback of the Audio Recorder from com.google.audio package.
 * Challenge is to isolate the sound of a ping pong ball hitting a ping pong / stone table.
 * Currently we're using thresholds suggested in https://www.tandfonline.com/doi/abs/10.1080/02640414.2018.1462578?journalCode=rjsp20&#:~:text=Using%20average%20values%20for%20density,ball%20is%20approximately%205880%20Hz
 * <p>
 * When a Bounce is detected, onAudioBounce is called.
 */
public class ImplAudioRecorderCallback implements com.google.audio.core.Callback {
    private static final int MAX_FREQUENCY = 13000;
    private static final int MIN_FREQUENCY = 8000;
    private static final int TIME_BETWEEN_TWO_BOUNCES_MS = 500;
    private final AudioCalculator audioCalculator;
    private final Handler handler;
    private final Callback callback;
    private long timestampLastDetectedBounce;

    public ImplAudioRecorderCallback(ImplAudioRecorderCallback.Callback callback) {
        this.callback = callback;
        this.audioCalculator = new AudioCalculator();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onBufferAvailable(byte[] buffer) {
        if(System.currentTimeMillis() - timestampLastDetectedBounce > TIME_BETWEEN_TWO_BOUNCES_MS) {
            audioCalculator.setBytes(buffer);
            audioCalculator.getAmplitude();
            final double frequency = audioCalculator.getFrequency();
            final double decibel = audioCalculator.getDecibel();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if ((frequency > MIN_FREQUENCY) && (frequency < MAX_FREQUENCY)) {
                        callback.onAudioBounceDetected();
                        timestampLastDetectedBounce = System.currentTimeMillis();
                    }
                }
            });
        }
    }

    public interface Callback {
        void onAudioBounceDetected();
    }
}
