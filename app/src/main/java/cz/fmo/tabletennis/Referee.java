package cz.fmo.tabletennis;

import com.android.grafika.Log;

import java.util.Timer;
import java.util.TimerTask;

import cz.fmo.Lib;
import cz.fmo.data.TrackSet;
import cz.fmo.events.EventDetectionCallback;
import cz.fmo.tabletennis.timeouts.OutOfFrameTimerTask;
import cz.fmo.tabletennis.timeouts.PauseTimerTask;
import helper.DirectionX;

public class Referee implements EventDetectionCallback, ScoreManipulationCallback {
    private static final int OUT_OF_FRAME_MAX_DELAY = 1500;
    private static final int PAUSE_DELAY = 1000;
    private Timer outOfFrameTimer;
    private Timer timeOutNextServeTimer;
    private GameCallback gameCallback;
    private Game currentGame;
    private Side currentStriker;
    private Side currentBallSide;
    private GameState state;
    private int bounces;
    private int serveCounter;

    public Referee(Side servingSide) {
        this.currentStriker = servingSide;
        this.currentBallSide = servingSide;
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

    public Side getCurrentStriker() {
        return currentStriker;
    }

    @Override
    public void onBounce(Lib.Detection detection) {
        switch (this.state) {
            case WAIT_FOR_SERVE:
                if ((detection.predecessor != null) && (Math.abs(detection.centerX-detection.predecessor.centerX)>20) &&
                        (getServer() == Side.LEFT && currentBallSide == Side.LEFT && detection.directionX == DirectionX.RIGHT) ||
                        (getServer()  == Side.RIGHT && currentBallSide == Side.RIGHT && detection.directionX == DirectionX.LEFT)) {
                    this.state = GameState.SERVING;
                    currentBallSide = getServer();
                }
                break;
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
                // do not change striker if the ball was sent back by net
                if (currentBallSide == side) {
                    bounces = 0;
                    currentStriker = side;
                }
                break;
            case SERVING:
                if (side != getServer()) {
                    bounces = 0;
                }
                currentStriker = side;
                break;
            default:
                currentStriker = side;
                break;
        }
    }

    @Override
    public void onNearlyOutOfFrame(Lib.Detection detection, Side side) {
        if(this.state == GameState.PLAY && side != Side.TOP)
            handleOutOfFrame();
    }

    @Override
    public void onStrikeFound(TrackSet tracks) {
        switch (this.state) {
            case WAIT_FOR_SERVE:
                Lib.Detection latestDetection = tracks.getTracks().get(0).getLatest();
                if (((getServer() == Side.LEFT && currentBallSide == Side.LEFT && latestDetection.directionX == DirectionX.RIGHT) ||
                        (getServer()  == Side.RIGHT && currentBallSide == Side.RIGHT && latestDetection.directionX == DirectionX.LEFT)) &&
                        (latestDetection.predecessor != null)) {
                    this.state = GameState.SERVING;
                    currentBallSide = getServer();
                    this.currentStriker = getServer();
                }
                break;
            case OUT_OF_FRAME:
                // if ball was out of frame for too long, a point would have been scored.
                this.outOfFrameTimer.cancel();
                this.outOfFrameTimer = null;
                this.state = GameState.PLAY;
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
                    //Log.d("Server fault: No Bounce on own Side");
                    //faultBySide(getServer());
                    //break;
                }
            case PLAY:
                this.state = GameState.PLAY;
                this.currentBallSide = side;
                this.bounces = 0;
                break;
            default:
                break;
        }
    }

    @Override
    public void onBallDroppedSideWays() {
        switch (this.state) {
            case PLAY:
                if (bounces == 0) {
                    Log.d("Fault by Striker: Ball has fallen off side ways and had no bounce");
                    faultBySide(currentStriker);
                } else if (bounces == 1) {
                    Log.d("Point by Striker: Ball has fallen off side ways and had a bounce");
                    pointBySide(currentStriker);
                }
                break;
        }
    }

    @Override
    public void onTimeout() {
        Log.d("Timeout (2 seconds since last valid detection)");
        if (this.state == GameState.PLAY)
            handleOutOfFrame();
    }

    @Override
    public void onPointDeduction(Side side) {
        gameCallback.onPointDeduction(side);
        initPoint();
    }

    @Override
    public void onPointAddition(Side side) {
        gameCallback.onPoint(side);
        initPoint();
    }

    public Side getCurrentBallSide() {
        return currentBallSide;
    }

    public void onOutOfFrameForTooLong() {
        if (this.state == GameState.OUT_OF_FRAME) {
            if(this.bounces == 1) {
                Log.d("Out of Frame for too long - Strike received no return");
                pointBySide(currentStriker);
            } else {
                Log.d("Out of Frame for too long - Striker did not bounce");
                faultBySide(currentStriker);
            }
        } else {
            this.outOfFrameTimer = null;
        }
    }

    public void onStartNextServe() {
        this.state = GameState.WAIT_FOR_SERVE;
    }

    private void handleOutOfFrame() {
        if(this.bounces == 0) {
            Log.d("No bounce and went out of frame");
            faultBySide(currentStriker);
        } else {
            // schedule out of frame timer
            TimerTask outOfFrameTask = new OutOfFrameTimerTask(this);
            outOfFrameTimer = new Timer("outOfFrameTimer");
            outOfFrameTimer.schedule(outOfFrameTask, OUT_OF_FRAME_MAX_DELAY);
            this.state = GameState.OUT_OF_FRAME;
        }
    }

    private void setTimeoutForNextServe() {
        this.state = GameState.PAUSE;
        TimerTask outOfFrameTask = new PauseTimerTask(this);
        this.timeOutNextServeTimer = new Timer("timeOutNextServeTimer");
        this.timeOutNextServeTimer.schedule(outOfFrameTask, PAUSE_DELAY);
    }

    private void pointBySide(Side side) {
        gameCallback.onPoint(side);
        initPoint();
        setTimeoutForNextServe();
    }

    private void faultBySide(Side side) {
        if (side == Side.RIGHT) {
            gameCallback.onPoint(Side.LEFT);
        } else {
            gameCallback.onPoint(Side.RIGHT);
        }
        initPoint();
        setTimeoutForNextServe();
    }

    private void initPoint() {
        this.bounces = 0;
        if (this.outOfFrameTimer != null) {
            this.outOfFrameTimer.cancel();
            this.outOfFrameTimer = null;
        }
        this.state = GameState.WAIT_FOR_SERVE;
        this.currentBallSide = getServer();
        this.currentStriker = getServer();
    }

    private void applyRuleSet() {
        if (bounces == 1) {
            if (this.currentStriker == this.currentBallSide) {
                Log.d("currentStriker: "+this.currentStriker);
                Log.d("currentBallSide: "+this.currentBallSide);
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
}
