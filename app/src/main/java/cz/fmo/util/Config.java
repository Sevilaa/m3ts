package cz.fmo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cz.fmo.R;

public class Config {
    private static final boolean IS_FRONT_FACING = false;
    private static final boolean IS_HIGH_RES = false;
    private static final boolean IS_SLOW_PREVIEW = false;
    private static final boolean IS_GRAY = true;
    private static final boolean IS_DISABLE_DETECTION = false;
    private static final VelocityEstimationMode VELOCITY_ESTIMATION_MODE = VelocityEstimationMode.KM_H;
    private static final int PROC_RES = 600;    // high processing res (detects small objects)
    private static final float OBJECT_RADIUS = 0.040f; //radius ping pong ball
    private static final float FRAME_RATE = 30f;
    private final String player1Name;
    private final String player2Name;
    private final boolean useDebug;
    private final boolean doRecordMatches;
    private final boolean usePubnub;
    private final boolean useBlackSide;
    private final boolean useAudio;

    public Config(Context ctx) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(ctx);
        player1Name = getPlayer1Name(p, ctx);
        player2Name = getPlayer2Name(p, ctx);
        useDebug = getDebug(p, ctx);
        doRecordMatches = getRecordMatches(p, ctx);
        usePubnub = getUsePubnub(p, ctx);
        useBlackSide = getUseBlackSide(p, ctx);
        useAudio = getUseAudio(p, ctx);
    }

    private boolean getUseBlackSide(SharedPreferences p, Context ctx) {
        return p.getBoolean(ctx.getString(R.string.prefUseBlackSideKey), false);
    }

    private boolean getUseAudio(SharedPreferences p, Context ctx) {
        return p.getBoolean(ctx.getString(R.string.prefUseAudioKey), true);
    }

    private boolean getUsePubnub(SharedPreferences p, Context ctx) {
        return p.getBoolean(ctx.getString(R.string.prefPubnubKey), false);
    }

    private String getPlayer1Name(SharedPreferences p, Context ctx) {
        return p.getString(ctx.getString(R.string.prefPlayer1Key), "Hans");
    }

    private String getPlayer2Name(SharedPreferences p, Context ctx) {
        return p.getString(ctx.getString(R.string.prefPlayer2Key), "Peter");
    }

    private boolean getDebug(SharedPreferences p, Context ctx) {
        return p.getBoolean(ctx.getString(R.string.prefDisplayDebugKey), false);
    }

    private boolean getRecordMatches(SharedPreferences p, Context ctx) {
        return p.getBoolean(ctx.getString(R.string.prefRecordKey), false);
    }

    public float getFrameRate() {
        return FRAME_RATE;
    }

    public VelocityEstimationMode getVelocityEstimationMode() {
        return VELOCITY_ESTIMATION_MODE;
    }

    public int getProcRes() {
        return PROC_RES;
    }

    public boolean isFrontFacing() {
        return IS_FRONT_FACING;
    }

    public boolean isHighRes() {
        return IS_HIGH_RES;
    }

    public boolean isSlowPreview() {
        return IS_SLOW_PREVIEW;
    }

    public boolean isGray() {
        return IS_GRAY;
    }

    public float getObjectRadius() {
        return OBJECT_RADIUS;
    }

    public boolean isDetectionDisabled() {
        return IS_DISABLE_DETECTION;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public boolean isUseDebug() {
        return useDebug;
    }

    public boolean doRecordMatches() {
        return doRecordMatches;
    }

    public boolean isUsingPubnub() {
        return usePubnub;
    }

    public boolean isUseBlackSide() {
        return useBlackSide;
    }

    public boolean isUseAudio() {
        return useAudio;
    }

    public enum VelocityEstimationMode {
        PX_FR,
        M_S,
        KM_H,
        MPH,
    }
}
