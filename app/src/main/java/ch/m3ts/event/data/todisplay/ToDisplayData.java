package ch.m3ts.event.data.todisplay;

import ch.m3ts.tabletennis.match.DisplayUpdateListener;

/**
 * Events which are useful for the score board (display device)
 */
public interface ToDisplayData {
    void call(DisplayUpdateListener displayUpdateListener);
}
