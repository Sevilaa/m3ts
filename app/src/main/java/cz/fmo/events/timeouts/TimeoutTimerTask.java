package cz.fmo.events.timeouts;

import com.android.grafika.Log;

import java.util.TimerTask;

import cz.fmo.events.EventDetector;

public class TimeoutTimerTask extends TimerTask {
    private EventDetector eventDetector;
    private int currentNumberOfDetections;

    public TimeoutTimerTask(EventDetector eventDetector, int currentNumberOfDetections) {
        this.eventDetector = eventDetector;
        this.currentNumberOfDetections = currentNumberOfDetections;
    }

    @Override
    public void run() {
        Log.d("if timeout then: " + currentNumberOfDetections + " == " + eventDetector.getNumberOfDetections());
        if (currentNumberOfDetections == eventDetector.getNumberOfDetections()) {
            Log.d("Timeout: currentNumberOfDetections == eventDetector.getNumberOfDetections()");
            eventDetector.callAllOnTimeout();
        }
    }
}
