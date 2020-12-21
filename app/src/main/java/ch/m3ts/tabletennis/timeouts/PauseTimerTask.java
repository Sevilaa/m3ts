package ch.m3ts.tabletennis.timeouts;

import java.util.TimerTask;

import ch.m3ts.tabletennis.match.referee.Referee;

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
