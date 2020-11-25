package cz.fmo.tabletennis.timeouts;

import java.util.TimerTask;

import cz.fmo.tabletennis.Referee;

public class PauseTimerTask extends TimerTask {
    private Referee referee;

    public PauseTimerTask(Referee referee) {
        this.referee = referee;
    }

    @Override
    public void run() {
        referee.resume();
    }
}
