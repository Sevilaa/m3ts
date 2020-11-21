package com.android.grafika;

import java.util.TimerTask;

public class DebugHandlerRefreshTimerTask extends TimerTask {
    private DebugHandler debugHandler;

    public DebugHandlerRefreshTimerTask(DebugHandler debugHandler) {
        this.debugHandler = debugHandler;
    }

    @Override
    public void run() {
        this.debugHandler.refreshDebugTextViews();
    }
}
