package ch.m3ts.tabletennis.events.timeouts;

import java.util.TimerTask;

import ch.m3ts.tabletennis.events.EventDetector;

public class TimeoutTimerTask extends TimerTask {
    private final EventDetector eventDetector;
    private final int currentNumberOfDetections;

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
