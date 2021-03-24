package ch.m3ts.tracker.visualization.live;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.google.audio.calculators.AudioCalculator;

/**
 * Callback of the Audio Recorder from com.google.audio package.
 * Challenge is to isolate the sound of a ping pong ball hitting a ping pong / stone table.
 * Currently we're using thresholds suggested in https://www.tandfonline.com/doi/abs/10.1080/02640414.2018.1462578?journalCode=rjsp20&#:~:text=Using%20average%20values%20for%20density,ball%20is%20approximately%205880%20Hz
 */
public class DebugImplAudioRecorderCallback implements com.google.audio.core.Callback {
    private static final int MAX_FREQUENCY = 13000;
    private static final int MIN_FREQUENCY = 8000;
    private static final int MIN_DECIBEL = -20;
    private static final int TIME_BETWEEN_TWO_BOUNCES_MS = 500;
    private final TextView txtAmp;
    private final TextView txtFrequency;
    private final TextView txtAudioBounce;
    private final AudioCalculator audioCalculator;
    private final Handler handler;
    private int bounces;
    private long timestampLastDetectedBounce;

    public DebugImplAudioRecorderCallback(TextView txtAmp, TextView txtFrequency, TextView txtAudioBounce) {
        this.txtAmp = txtAmp;
        this.txtFrequency = txtFrequency;
        this.txtAudioBounce = txtAudioBounce;
        this.audioCalculator = new AudioCalculator();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onBufferAvailable(byte[] buffer) {
        audioCalculator.setBytes(buffer);
        final int amplitude = audioCalculator.getAmplitude();
        final double frequency = audioCalculator.getFrequency();
        final double decibel = audioCalculator.getDecibel();

        final String amp = String.valueOf(amplitude + " Amp");
        final String hz = String.valueOf(frequency + " Hz");
        final String db = String.valueOf(decibel + " db");

        handler.post(new Runnable() {
            @Override
            public void run() {
                txtAmp.setText(db);
                if ((frequency > MIN_FREQUENCY) && (frequency < MAX_FREQUENCY) && decibel > MIN_DECIBEL &&
                        System.currentTimeMillis() - timestampLastDetectedBounce > TIME_BETWEEN_TWO_BOUNCES_MS) {
                    ++bounces;
                    txtFrequency.setTextColor(Color.GREEN);
                    txtAudioBounce.setText(String.valueOf(bounces));
                    timestampLastDetectedBounce = System.currentTimeMillis();
                } else {
                    txtFrequency.setTextColor(Color.BLACK);
                }
                txtFrequency.setText(hz);
            }
        });
    }
}
