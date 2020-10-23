package helper;

import android.graphics.Point;

import cz.fmo.tabletennis.Table;

public class TableGenerator {

    /**
     * @return Table with the same corner and net points as in the garage.mp4 recording.
     */
    public static Table makeTableFromGarageRecording() {
        return  new Table(new Point[]{new Point(45,904), new Point(1810, 921), new Point(1388, 723), new Point(471, 709)},
                new Point[]{new Point(959,927), new Point(939, 723)});
    }
}
