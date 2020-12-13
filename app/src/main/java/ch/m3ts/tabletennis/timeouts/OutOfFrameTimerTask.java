package ch.m3ts.tabletennis.timeouts;

import java.util.TimerTask;

import ch.m3ts.tabletennis.match.referee.Referee;


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
