package ch.m3ts.event.data.todisplay;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;

public class ScoreData implements ToDisplayData {
    private final Side scorer;
    private final int score;
    private final Side nextServer;
    private final Side lastServer;

    public ScoreData(Side scorer, int score, Side nextServer, Side lastServer) {
        this.scorer = scorer;
        this.score = score;
        this.nextServer = nextServer;
        this.lastServer = lastServer;
    }

    @Override
    public void call(DisplayUpdateListener displayUpdateListener) {
        displayUpdateListener.onScore(scorer, score, nextServer, lastServer);
    }

    public Side getLastServer() {
        return lastServer;
    }
}
