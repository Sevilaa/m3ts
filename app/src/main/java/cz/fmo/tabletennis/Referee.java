package cz.fmo.tabletennis;

import com.android.grafika.Log;

import cz.fmo.Lib;
import cz.fmo.data.TrackSet;
import cz.fmo.events.EventDetectionCallback;
import helper.DirectionX;

public class Referee implements EventDetectionCallback, ScoreManipulationCallback {
    private GameCallback gameCallback;
    private Game currentGame;
    private Side currentStriker;
    private Side currentBallSide;
    private GameState state;
    private int bounces;
    private int serveCounter;
    private long outOfFrameTimestamp;

    public Referee(Side servingSide) {
        this.currentStriker = servingSide;
        this.currentBallSide = null;
        this.serveCounter = 0;
        this.bounces = 0;
        this.state = GameState.WAIT_FOR_SERVE;
    }

    public void setGame(Game game) {
        this.gameCallback = game;
        this.currentGame = game;
    }

    public GameState getState() {
        return state;
    }

    public Side getServer() { return currentGame.getServer(); }

    @Override
    public void onBounce(Lib.Detection detection) {
        switch (this.state) {
            case SERVING:
                bounces++;
                applyRuleSetServing();
                break;
            case PLAY:
                bounces++;
                applyRuleSet();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSideChange(Side side) {
        switch (this.state) {
            case PLAY:
            case SERVING:
                bounces = 0;
                currentStriker = side;
                break;
            default:
                break;
        }
    }

    @Override
    public void onNearlyOutOfFrame(Lib.Detection detection, Side side) {
        if(this.state == GameState.PLAY) {
            if(this.bounces == 0) {
                faultBySide(currentStriker);
            } else {
                this.state = GameState.OUT_OF_FRAME;
                this.outOfFrameTimestamp = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onStrikeFound(TrackSet tracks) {
        switch (this.state) {
            case WAIT_FOR_SERVE:
                if ((getServer() == Side.LEFT && tracks.getTracks().get(0).getLatest().directionX == DirectionX.RIGHT) ||
                        (getServer()  == Side.RIGHT && tracks.getTracks().get(0).getLatest().directionX == DirectionX.LEFT)) {
                    this.state = GameState.SERVING;
                    currentBallSide = getServer();
                }
                break;
            case OUT_OF_FRAME:
                if(isOutOfFrameForTooLong()) {
                    if(this.bounces == 1) {
                        pointBySide(currentStriker);
                    } else {
                        faultBySide(currentStriker);
                    }

                } else {
                    this.state = GameState.PLAY;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onTableSideChange(Side side) {
        switch (this.state) {
            case SERVING:
                if(bounces == 0) {
                    faultBySide(getServer());
                    break;
                }
            case PLAY:
                this.state = GameState.PLAY;
                currentBallSide = side;
                bounces = 0;
                break;
            default:
                break;
        }
    }

    @Override
    public void onPointDeduction(Side side) {
        gameCallback.onPointDeduction(side);
        initPoint();
    }

    @Override
    public void onPointAddition(Side side) {
        pointBySide(side);
    }

    private void pointBySide(Side side) {
        gameCallback.onPoint(side);
        initPoint();
    }

    private void faultBySide(Side side) {
        if (side == Side.RIGHT) {
            gameCallback.onPoint(Side.LEFT);
        } else {
            gameCallback.onPoint(Side.RIGHT);
        }
        initPoint();
    }

    private void initPoint() {
        this.bounces = 0;
        this.state = GameState.WAIT_FOR_SERVE;
        currentBallSide = getServer();
        currentStriker = getServer();
    }

    private void applyRuleSet() {
        if (bounces == 1) {
            if (this.currentStriker == this.currentBallSide) {
                Log.d("Bounce on same Side");
                faultBySide(this.currentStriker);
            }
        } else if (bounces >= 2) {
            if (this.currentStriker != this.currentBallSide) {
                Log.d("Double Bounce");
                pointBySide(this.currentStriker);
            }
        }
    }

    private void applyRuleSetServing() {
        if (bounces > 1 && currentBallSide == getServer()) {
            Log.d("Server Fault: Multiple Bounces on same Side");
            faultBySide(getServer());
        }
    }

    private boolean isOutOfFrameForTooLong() {
        return System.currentTimeMillis() - this.outOfFrameTimestamp > 1500;
    }
}
