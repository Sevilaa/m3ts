package cz.fmo.tabletennis.timeouts;

import java.util.TimerTask;

import cz.fmo.tabletennis.Referee;


public class OutOfFrameTimerTask extends TimerTask {
    private Referee referee;

    public OutOfFrameTimerTask(Referee referee) {
        this.referee = referee;
    }

    @Override
    public void run() {
        referee.onOutOfFrameForTooLong();
    }
}
