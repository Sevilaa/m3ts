package cz.fmo.tabletennis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cz.fmo.Lib;
import cz.fmo.data.TrackSet;
import cz.fmo.util.Config;
import helper.DetectionGenerator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Before
    public void setUp(){
        mockConfig = mock(Config.class);
        when(mockConfig.isDisableDetection()).thenReturn(false);
        when(mockConfig.getFrameRate()).thenReturn(30f);
        when(mockConfig.getVelocityEstimationMode()).thenReturn(Config.VelocityEstimationMode.PX_FR);
        when(mockConfig.getObjectRadius()).thenReturn(10f);
        realTrackSet.setConfig(mockConfig);
        referee = new Referee(STARTING_SIDE);
        gameMock = mock(Game.class);
        when(gameMock.getServer()).thenReturn(Side.LEFT);
        referee.setGame(gameMock);
        detection = new Lib.Detection();
    }

    @Test
    public void onServingAceDoubleBounce() {
        assertEquals(referee.getState(), GameState.WAIT_FOR_SERVE);
        simulateServe();
        assertEquals(referee.getState(), GameState.SERVING);
        referee.onBounce(detection, Side.LEFT);
        referee.onTableSideChange(Side.RIGHT);
        assertEquals(referee.getState(), GameState.PLAY);
        referee.onBounce(detection, Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        verify(gameMock, times(1)).onPoint(STARTING_SIDE);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        assertEquals(referee.getState(), GameState.PAUSE);
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
            e.printStackTrace();
        }
        referee.onStrikeFound(realTrackSet.getTracks().get(0));
        verify(gameMock, times(1)).onPoint(STARTING_SIDE);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        assertEquals(referee.getState(), GameState.PAUSE);
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
            e.printStackTrace();
        }
        referee.onStrikeFound(realTrackSet.getTracks().get(0));
        referee.onSideChange(Side.RIGHT);
        referee.onTableSideChange(Side.LEFT);
        referee.onBounce(detection, Side.LEFT);
        verify(gameMock, times(0)).onPoint(STARTING_SIDE);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        assertEquals(referee.getState(), GameState.PLAY);
    }

    @Test
    public void onServingDoubleBounceOnOwnSideFault() {
        simulateServe();
        referee.onBounce(detection, Side.LEFT);
        referee.onBounce(detection, Side.LEFT);
        verify(gameMock, times(1)).onPoint(Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.LEFT);
        assertEquals(referee.getState(), GameState.PAUSE);
    }

    @Test
    public void onServingWithoutBounceFault() {
        // will be ignored at the moment
        simulateServe();
        referee.onTableSideChange(Side.RIGHT);
        referee.onBounce(detection, Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.RIGHT);
        verify(gameMock, times(0)).onPoint(Side.LEFT);
        assertEquals(referee.getState(), GameState.PLAY);
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
        assertEquals(referee.getState(), GameState.PLAY);
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
            e.printStackTrace();
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

    @After
    public void tearDown() {
        referee = null;
        gameMock = null;
    }

    private void simulateServe() {
        long detectionTime = System.nanoTime();
        int delay = 1000/FRAME_RATE;
        Lib.Detection[] someDetections = DetectionGenerator.makeDetectionsInXDirectionOnTable(true);
        for (Lib.Detection someDetection : someDetections) {
            detectionTime = detectionTime + delay;
            realTrackSet.addDetections(new Lib.Detection[]{someDetection}, SOME_WIDTH, SOME_HEIGHT, detectionTime);
            referee.onStrikeFound(realTrackSet.getTracks().get(0));
        }
    }
}