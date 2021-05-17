package ch.m3ts.eventbus.data;

import ch.m3ts.display.stats.MatchStats;

public class StatsData {
    private MatchStats stats;

    public StatsData(MatchStats stats) {
        this.stats = stats;
    }

    public MatchStats getStats() {
        return stats;
    }
}
