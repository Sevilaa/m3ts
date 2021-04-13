package ch.m3ts.util;

import ch.m3ts.tabletennis.helper.Side;

public class CSVStringBuilder {
    private static final String SEPARATOR = ";";
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

    @Override
    public String toString() {
        return csv.toString();
    }
}
