package ch.m3ts.tabletennis.helper;


import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class Duration {
    private long start;
    private long end = 0;

    public Duration() {
        this.start = new Timestamp(System.currentTimeMillis()).getTime();
    }

    public void stop() {
        this.end = new Timestamp(System.currentTimeMillis()).getTime();
    }

    public void reset() {
        this.end = 0;
        this.start = new Timestamp(System.currentTimeMillis()).getTime();
    }

    public int getSeconds() {
        long end = this.end;
        if(end == 0) {
            end = new Timestamp(System.currentTimeMillis()).getTime();
        }
        return (int) TimeUnit.MILLISECONDS.toSeconds(end - this.start);
    }
}
