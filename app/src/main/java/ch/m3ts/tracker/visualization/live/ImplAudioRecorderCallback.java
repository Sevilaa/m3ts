package ch.m3ts.tracker.visualization.live;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.google.audio.calculators.AudioCalculator;

public class ImplAudioRecorderCallback implements com.google.audio.core.Callback {
    private TextView txtAmp;
    private TextView txtFrequency;
    private AudioCalculator audioCalculator;
    private Handler handler;

    public ImplAudioRecorderCallback(TextView txtAmp, TextView txtFrequency) {
        this.txtAmp = txtAmp;
        this.txtFrequency = txtFrequency;
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
                if ((frequency > 7500) && decibel > -20.0) {
                    txtFrequency.setTextColor(Color.GREEN);
                } else {
                    txtFrequency.setTextColor(Color.BLACK);
                }
                txtFrequency.setText(hz);
            }
        });
    }
}
