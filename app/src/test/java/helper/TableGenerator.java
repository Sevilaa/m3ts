package helper;

import android.graphics.Point;

import ch.m3ts.util.Table;

public class TableGenerator {

    /**
     * @return Table with the same corner and net points as in the garage.mp4 recording.
     */
    public static Table makeTableFromGarageRecording() {
        return  new Table(new Point[]{new Point(45,904), new Point(1810, 921)}, new Point(959,927));
    }
}
