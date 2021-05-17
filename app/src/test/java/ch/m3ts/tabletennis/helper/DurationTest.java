package ch.m3ts.tabletennis.helper;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class DurationTest {
    private Duration duration;
    private Random rd;
    private int sleepTime;

    @Before
    public void setUp() throws Exception {
        this.rd = new Random();
        this.sleepTime = rd.nextInt(4000);
        this.duration = new Duration();
    }

    @Test
    public void getSecondsAndStop() throws InterruptedException {
        Thread.sleep(sleepTime);
        assertEquals(sleepTime/1000, this.duration.getSeconds());
        Thread.sleep(1000);
        this.duration.stop();
        assertEquals(sleepTime/1000 + 1, this.duration.getSeconds());
    }

    @Test
    public void reset() throws InterruptedException {
        Thread.sleep(sleepTime);
        assertEquals(sleepTime/1000, duration.getSeconds());
        this.duration.reset();
        assertEquals(0, duration.getSeconds());
    }
}