package ch.m3ts.eventbus.data;

import ch.m3ts.display.stats.data.MatchData;

public class StatsData {
    private MatchData stats;

    public StatsData(MatchData stats) {
        this.stats = stats;
    }

    public MatchData getStats() {
        return stats;
    }
}
