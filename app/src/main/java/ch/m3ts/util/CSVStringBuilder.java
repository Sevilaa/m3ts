package ch.m3ts.util;

import java.util.List;

import ch.m3ts.tabletennis.helper.Side;
import cz.fmo.Lib;
import cz.fmo.data.Track;

public class CSVStringBuilder {
    private static final String SEPARATOR = ";";
    private static final String IN_CELL_SEPARATOR = "_";
    private final StringBuilder csv;

    public CSVStringBuilder() {
        csv = new StringBuilder();
    }

    public static CSVStringBuilder builder() {
        return new CSVStringBuilder();
    }

    public CSVStringBuilder add(String value) {
        csv.append(value);
        csv.append(SEPARATOR);
        return this;
    }

    public CSVStringBuilder add(Side side) {
        return add(side.toString());
    }

    public CSVStringBuilder add(int number) {
        return add(String.valueOf(number));
    }

    public CSVStringBuilder add(List<Track> tracks) {
        int trackId = 0;
        for (Track track : tracks) {
            Lib.Detection latest = track.getLatest();
            while (latest != null) {
                add(trackId + IN_CELL_SEPARATOR + latest.centerX + IN_CELL_SEPARATOR + latest.centerY + IN_CELL_SEPARATOR + latest.centerZ + IN_CELL_SEPARATOR + latest.velocity + IN_CELL_SEPARATOR + latest.isBounce);
                latest = latest.predecessor;
            }
            trackId++;
        }
        return this;
    }

    @Override
    public String toString() {
        return csv.toString();
    }
}
