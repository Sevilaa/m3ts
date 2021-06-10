package ch.m3ts.eventbus.event;

import ch.m3ts.display.statistic.data.MatchData;

public class StatsData {
    private MatchData stats;

    public StatsData(MatchData stats) {
        this.stats = stats;
    }

    public MatchData getStats() {
        return stats;
    }
}
