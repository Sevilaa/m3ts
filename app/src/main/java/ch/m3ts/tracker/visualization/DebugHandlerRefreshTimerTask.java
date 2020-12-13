package ch.m3ts.tracker.visualization;

import java.util.TimerTask;

public class DebugHandlerRefreshTimerTask extends TimerTask {
    private MatchVisualizeHandler matchVisualizeHandler;

    public DebugHandlerRefreshTimerTask(MatchVisualizeHandler matchVisualizeHandler) {
        this.matchVisualizeHandler = matchVisualizeHandler;
    }

    @Override
    public void run() {
        this.matchVisualizeHandler.refreshDebugTextViews();
    }
}
