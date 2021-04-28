package ch.m3ts.tabletennis.match.referee;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.Subscribable;
import ch.m3ts.eventbus.TTEvent;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.data.todisplay.ReadyToServeData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import ch.m3ts.tabletennis.match.game.Game;
import ch.m3ts.util.Log;
import cz.fmo.Lib;
import cz.fmo.data.TrackSet;
import cz.fmo.util.Config;
import helper.DetectionGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StubListener implements Subscribable {
    private final DisplayUpdateListener displayUpdateListener;

    StubListener(DisplayUpdateListener displayUpdateListener) {
        this.displayUpdateListener = displayUpdateListener;
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof ReadyToServeData) {
            ReadyToServeData readyToServeData = (ReadyToServeData) data;
            readyToServeData.call(displayUpdateListener);
        }
    }
}

public class RefereeTest {
    private Referee referee;
    private Game gameMock;
    private static final int FRAME_RATE = 30;
    private static final int SOME_WIDTH = 1920;
    private static final int SOME_HEIGHT = 1080;
    private TrackSet realTrackSet = TrackSet.getInstance();
    private Config mockConfig;
    private static final Side STARTING_SIDE = Side.LEFT;
    private Lib.Detection detection;
    private StubListener stubListener;
    private DisplayUpdateListener displayUpdateListener;

    @Before
    public void setUp() {
        displayUpdateListener = mock(DisplayUpdateListener.class);
        mockConfig = mock(Config.class);
        when(mockConfig.isDetectionDisabled()).thenReturn(false);
        when(mockConfig.getFrameRate()).thenReturn(30f);
        when(mockConfig.getVelocityEstimationMode()).thenReturn(Config.VelocityEstimationMode.PX_FR);
        when(mockConfig.getObjectRadius()).thenReturn(10f);
        realTrackSet.setConfig(mockConfig);
        referee = new Referee(STARTING_SIDE);
        gameMock = mock(Game.class);
        when(gameMock.getServer()).thenReturn(Side.LEFT);
        referee.setGame(gameMock, true);
        detection = new Lib.Detection();
        stubListener = new StubListener(displayUpdateListener);
        TTEventBus.getInstance().register(stubListener);
    }

    @After
    public void cleanUp() {
        TTEventBus.getInstance().unregister(stubListener);
    }

    @Test
    public void onServingAceDoubleBounce() {
        assertEquals(State.WAIT_FOR_SERVE, referee.getState());
        simulateServe();
        assertEquals(State.SERVING, referee.getState());
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        assertEquals(State.PLAY, referee.getState());
        referee.onBounce(detection, Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        verify(gameMock, times(1)).onPoint(STARTING_SIDE);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        assertEquals(State.PAUSE, referee.getState());
    }

    @Test
    public void onServingAceOutOfFrame() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onNearlyOutOfFrame(detection, Side.RIGHT);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.d(e.getMessage());
        }
        referee.onStrikeFound(realTrackSet.getTracks().get(0));
        verify(gameMock, times(1)).onPoint(STARTING_SIDE);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        assertEquals(State.PAUSE, referee.getState());
    }

    @Test
    public void onServingValidOutOfFrame() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onNearlyOutOfFrame(detection, Side.RIGHT);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d(e.getMessage());
        }
        referee.onStrikeFound(realTrackSet.getTracks().get(0));
        referee.onSideChange(Side.RIGHT);
        referee.onTableSideChange(Side.LEFT);
        referee.onBounce(detection, Side.LEFT);
        verify(gameMock, times(0)).onPoint(STARTING_SIDE);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        assertEquals(State.PLAY, referee.getState());
    }

    @Test
    public void onServingDoubleBounceOnOwnSideFault() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onBounce(detection, Side.LEFT);
        verify(gameMock, times(1)).onPoint(Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.LEFT);
        assertEquals(State.PAUSE, referee.getState());
    }

    @Test
    public void onServingWithoutBounceFault() {
        // will be ignored at the moment
        simulateServe();
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.LEFT);
        assertEquals(State.PLAY, referee.getState());
    }

    @Test
    public void onServingValid() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onSideChange(Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.LEFT);
        assertEquals(State.PLAY, referee.getState());
    }


    @Test
    public void onServingWithTimeout() {
        // simulates a serve which gets shot into the net -> fault by server
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTimeout();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.d(e.getMessage());
        }
        verify(gameMock, times(1)).onPoint(Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.LEFT);
    }

    @Test
    public void onShootingTheBallIntoNet() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onSideChange(Side.RIGHT);
        referee.onTimeout();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.d(e.getMessage());
        }
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        verify(gameMock, times(1)).onPoint(Side.LEFT);
    }

    @Test
    public void onReturnFaultBounceOnOwnSide() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        verify(gameMock, times(1)).onPoint(Side.LEFT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
    }

    @Test
    public void onReturnFaultNoBounceAndTooLongOutOfFrame() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onSideChange(Side.RIGHT);
        referee.onTableSideChange(Side.LEFT);
        referee.onNearlyOutOfFrame(detection, Side.LEFT);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.d(e.getMessage());
        }
        referee.onStrikeFound(realTrackSet.getTracks().get(0));
        verify(gameMock, times(1)).onPoint(Side.LEFT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
    }

    @Test
    public void onReturnWithOnlyAudioBounceAndTooLongOutOfFrame() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onSideChange(Side.RIGHT);
        referee.onTableSideChange(Side.LEFT);
        referee.onAudioBounce(Side.LEFT);
        referee.onNearlyOutOfFrame(detection, Side.LEFT);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.d(e.getMessage());
        }
        referee.onStrikeFound(realTrackSet.getTracks().get(0));
        verify(gameMock, times(0)).onPoint(Side.LEFT);
        verify(gameMock, times(1)).onPoint(Side.RIGHT);
    }

    @Test
    public void onReturnFaultWithOnlyAudioBounceAndTooLongOutOfFrame() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onSideChange(Side.RIGHT);
        // some other noise (f.e. racket hitting the ball) got heard as audio bounce
        referee.onAudioBounce(Side.LEFT);
        referee.onTableSideChange(Side.LEFT);
        referee.onNearlyOutOfFrame(detection, Side.LEFT);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.d(e.getMessage());
        }
        referee.onStrikeFound(realTrackSet.getTracks().get(0));
        verify(gameMock, times(1)).onPoint(Side.LEFT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
    }

    @Test
    public void onReturnFaultNoBounceAndOutOfFrameButInstantBack() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onSideChange(Side.RIGHT);
        referee.onTableSideChange(Side.LEFT);
        referee.onNearlyOutOfFrame(detection, Side.LEFT);
        referee.onStrikeFound(realTrackSet.getTracks().get(0));
        verify(gameMock, times(0)).onPoint(Side.LEFT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
    }

    @Test
    public void onFallingOffSideWays() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onSideChange(Side.RIGHT);
        referee.onBallDroppedSideWays();
        verify(gameMock, times(1)).onPoint(Side.LEFT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
    }

    @Test
    public void onFallingOffSideWaysWithBounce() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onSideChange(Side.RIGHT);
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        referee.onBallDroppedSideWays();
        verify(gameMock, times(1)).onPoint(Side.LEFT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
    }

    @Test
    public void testPointManipulation() {
        Subscribable subscribable = mock(Subscribable.class);
        ArgumentCaptor<TTEvent> argumentCaptor = ArgumentCaptor.forClass(TTEvent.class);
        TTEventBus.getInstance().register(subscribable);

        assertSame(State.WAIT_FOR_SERVE, referee.getState());
        verify(gameMock, times(0)).onPoint(Side.LEFT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        verify(gameMock, times(0)).onPointDeduction(Side.LEFT);
        verify(gameMock, times(0)).onPointDeduction(Side.RIGHT);
        referee.onPointAddition(Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.LEFT);
        verify(gameMock, times(1)).onPoint(Side.RIGHT);
        assertSame(State.PAUSE, referee.getState());
        referee.onPointDeduction(Side.RIGHT);
        verify(gameMock, times(0)).onPointDeduction(Side.LEFT);
        verify(gameMock, times(1)).onPointDeduction(Side.RIGHT);
        assertSame(State.PAUSE, referee.getState());
        referee.onPause();
        referee.onResume();
        verify(displayUpdateListener, times(1)).onReadyToServe(STARTING_SIDE);
        assertSame(State.WAIT_FOR_SERVE, referee.getState());
    }

    @After
    public void tearDown() {
        referee = null;
        gameMock = null;
    }

    private void simulateServe() {
        long detectionTime = System.nanoTime();
        int delay = 1000 / FRAME_RATE;
        Lib.Detection[] someDetections = DetectionGenerator.makeDetectionsInXDirectionOnTable(true);
        for (Lib.Detection someDetection : someDetections) {
            detectionTime = detectionTime + delay;
            realTrackSet.addDetections(new Lib.Detection[]{someDetection}, SOME_WIDTH, SOME_HEIGHT, detectionTime);
            referee.onStrikeFound(realTrackSet.getTracks().get(0));
        }
    }
}