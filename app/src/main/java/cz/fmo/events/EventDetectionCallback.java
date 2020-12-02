package cz.fmo.events;

import cz.fmo.Lib;
import cz.fmo.data.Track;
import cz.fmo.tabletennis.Side;

public interface EventDetectionCallback {
    void onBounce(Lib.Detection detection);
    void onSideChange(Side side);
    void onNearlyOutOfFrame(Lib.Detection detection, Side side);
    void onStrikeFound(Track track);
    void onTableSideChange(Side side);
    void onBallDroppedSideWays();
    void onTimeout();
}
