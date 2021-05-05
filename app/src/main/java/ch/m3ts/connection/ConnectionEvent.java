package ch.m3ts.connection;

public final class ConnectionEvent {
    public static final String MATCH_ENDED = "onMatchEnded";
    public static final String SCORE = "onScore";
    public static final String WIN = "onWin";
    public static final String READY_TO_SERVE = "onReadyToServe";
    public static final String NOT_READY_BUT_PLAYING = "onNotReadyButPlaying";
    public static final String STATUS_UPDATE = "onStatusUpdate";
    public static final String CONNECTION = "onConnected";
    public static final String TABLE_FRAME = "onTableFrame";
    public static final String STATS_PART = "onStatsPart";
    public static final String POINT_DEDUCTION = "onPointDeduction";
    public static final String POINT_ADDITION = "onPointAddition";
    public static final String STATUS_REQUEST = "requestStatus";
    public static final String STATS_REQUEST = "requestStats";
    public static final String PAUSE = "onPause";
    public static final String RESUME = "onResume";
    public static final String TABLE_FRAME_REQUEST = "onRequestTableFrame";
    public static final String TABLE_CORNER_SELECTION = "onSelectTableCorner";
    public static final String MATCH_START = "onStartMatch";
    public static final String MATCH_RESTART = "onRestartMatch";


    private ConnectionEvent() {
    }
}
