package cz.fmo.events.timeouts;

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
        if (currentNumberOfDetections == eventDetector.getNumberOfDetections()) {
            eventDetector.callAllOnTimeout();
        }
    }
}
