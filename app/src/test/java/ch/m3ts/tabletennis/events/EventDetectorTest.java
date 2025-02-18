package ch.m3ts.tabletennis.events;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.m3ts.detection.EventDetectionListener;
import ch.m3ts.detection.EventDetector;
import ch.m3ts.detection.ZPositionCalc;
import ch.m3ts.eventbus.Event;
import ch.m3ts.eventbus.Subscribable;
import ch.m3ts.eventbus.TTEventBus;
import ch.m3ts.eventbus.event.ball.EventDetectorEventData;
import ch.m3ts.util.Side;
import ch.m3ts.util.Table;
import cz.fmo.Lib;
import cz.fmo.data.Track;
import cz.fmo.data.TrackSet;
import cz.fmo.util.Config;
import helper.DetectionGenerator;
import helper.TableGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StubListener implements Subscribable {
    private final EventDetectionListener eventDetectionListener;

    public StubListener(EventDetectionListener eventDetectionListener) {
        this.eventDetectionListener = eventDetectionListener;
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof EventDetectorEventData) {
            EventDetectorEventData eventDetectorEventData = (EventDetectorEventData) data;
            eventDetectorEventData.call(eventDetectionListener);
        }
    }
}

public class EventDetectorTest {
    private Config mockConfig;
    private Table table;
    private EventDetectionListener mockCallback;
    private TrackSet mockTracks;
    private ZPositionCalc mockCalc;
    private StubListener stubListener;
    private static final TrackSet realTrackSet = TrackSet.getInstance();
    private static final int SOME_WIDTH = 1920;
    private static final int SOME_HEIGHT = 1080;
    private static final int FRAME_RATE = 30;

    @Before
    public void prepare() {
        table = TableGenerator.makeTableFromGarageRecording();
        mockConfig = mock(Config.class);
        mockTracks = mock(TrackSet.class);
        mockCallback = mock(EventDetectionListener.class);
        mockCalc = mock(ZPositionCalc.class);
        when(mockConfig.isDetectionDisabled()).thenReturn(false);
        when(mockConfig.getFrameRate()).thenReturn(30f);
        when(mockConfig.getVelocityEstimationMode()).thenReturn(Config.VelocityEstimationMode.PX_FR);
        when(mockConfig.getObjectRadius()).thenReturn(10f);
        when(mockCalc.getMmPerPixelFrontEdge()).thenReturn(99999.9);
        when(mockCalc.isBallZPositionOnTable(anyDouble())).thenReturn(true);
        when(mockCalc.getVideoWidthPx()).thenReturn(1280.0);
        stubListener = new StubListener(mockCallback);
        TTEventBus.getInstance().register(stubListener);
    }

    @After
    public void cleanUp() {
        TTEventBus.getInstance().unregister(stubListener);
    }

    @Test
    public void testUpdateTrackSetConfigOnConstruction() {
        EventDetector ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, mockTracks, table, mockCalc);
        verify(mockTracks, atLeastOnce()).setConfig(mockConfig);
    }

    @Test
    public void testUpdateTrackSetOnObjectsDetected() {
        EventDetector ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, mockTracks, table, mockCalc);
        Lib.Detection[] someDetections = new Lib.Detection[0];
        long detectionTime = System.nanoTime();
        ev.onObjectsDetected(someDetections, detectionTime);
        verify(mockTracks, times(1)).addDetections(someDetections, SOME_WIDTH, SOME_HEIGHT, detectionTime);
    }

    @Test
    public void testAudioBounceDetection() {
        EventDetector ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, TrackSet.getInstance(), table, mockCalc);
        Lib.Detection[] strikeDetectionsRight = DetectionGenerator.makeDetectionsInXDirectionOnTable(true);
        verify(mockCallback, times(0)).onAudioBounce(Side.RIGHT);
        verify(mockCallback, times(0)).onAudioBounce(Side.LEFT);
        invokeOnObjectDetectedWithDelay(strikeDetectionsRight, ev, 0);
        ev.onAudioBounceDetected();
        verify(mockCallback, times(1)).onAudioBounce(Side.RIGHT);

        // now simulate the ball going out of the frame and bouncing again
        // (f.e. ball is bouncing off the floor)
        try {
            Thread.sleep(3000);
            ev.onAudioBounceDetected();
            ev.onAudioBounceDetected();
            ev.onAudioBounceDetected();
            // should make no difference
            verify(mockCallback, times(1)).onAudioBounce(Side.RIGHT);
            verify(mockCallback, times(0)).onAudioBounce(Side.LEFT);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testOnStrikeFound() {
        EventDetector ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, realTrackSet, table, mockCalc);
        Lib.Detection[] strikeDetections = DetectionGenerator.makeDetectionsInXDirectionOnTable(true);
        invokeOnObjectDetectedWithDelay(strikeDetections, ev, 0);

        // assert that we have now one track with all detections in it
        verify(mockCallback, atLeastOnce()).onStrikeFound(realTrackSet.getTracks().get(0));
        assertEquals(1, realTrackSet.getTracks().size());
        Track track = realTrackSet.getTracks().get(0);
        Lib.Detection detection = track.getLatest();
        int count = strikeDetections.length-1;
        while(detection != null) {
            assertEquals(detection.id, strikeDetections[count].id);
            count--;
            detection = detection.predecessor;
        }
    }

    @Test
    public void testOnSideChange() {
        EventDetector ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, TrackSet.getInstance(), table, mockCalc);
        Lib.Detection[] strikeDetectionsRight = DetectionGenerator.makeDetectionsInXDirectionOnTable(true);
        Lib.Detection[] strikeDetectionsLeft = DetectionGenerator.makeDetectionsInXDirectionOnTable(false);
        // object is getting shot from left to right side
        invokeOnObjectDetectedWithDelay(strikeDetectionsRight, ev, 0);
        verify(mockCallback, times(0)).onSideChange(Side.RIGHT);
        verify(mockCallback, times(1)).onSideChange(Side.LEFT);
        // object is getting shot from right to left side
        invokeOnObjectDetectedWithDelay(strikeDetectionsLeft, ev, strikeDetectionsRight.length*2);      // need to add additional lag so that the detection won't get filtered out as noise
        verify(mockCallback, times(1)).onSideChange(Side.RIGHT);
        verify(mockCallback, times(1)).onSideChange(Side.LEFT);
        // object again getting shot from right to left side (same direction "edge" case)
        invokeOnObjectDetectedWithDelay(strikeDetectionsLeft, ev, strikeDetectionsRight.length*6);      // need to add additional lag so that the detection won't get filtered out as noise
        verify(mockCallback, times(2)).onSideChange(Side.RIGHT);
        verify(mockCallback, times(1)).onSideChange(Side.LEFT);
    }

    @Test
    public void testOnNearlyOutOfFrame() {
        TrackSet ts = TrackSet.getInstance();
        EventDetector ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, ts, table, mockCalc);
        Lib.Detection[] nearlyOutOfFrame;
        Lib.Detection predecessor = new Lib.Detection();
        predecessor.centerX = SOME_WIDTH / 2;
        predecessor.centerY = SOME_HEIGHT / 3;
        for(int i = 0; i < 10; i++) {
            nearlyOutOfFrame = DetectionGenerator.makeNearlyOutOfFrameDetections(ev.getNearlyOutOfFrameThresholds(), SOME_WIDTH, SOME_HEIGHT);
            for(Lib.Detection detection : nearlyOutOfFrame) {
                detection.predecessor = predecessor;
                detection.predecessorId = predecessor.predecessorId;
                invokeOnObjectDetectedWithDelay(new Lib.Detection[]{predecessor}, ev, i);
                invokeOnObjectDetectedWithDelay(new Lib.Detection[]{detection}, ev, i);
                verify(mockCallback, times(1)).onNearlyOutOfFrame(detection, DetectionGenerator.getNearlyOutOfFrameSide(ev.getNearlyOutOfFrameThresholds(), detection));
                ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, ts, table, mockCalc);
            }
        }
        Lib.Detection[] detectionsInFrame = DetectionGenerator.makeDetectionsInXDirectionOnTable(true);
        invokeOnObjectDetectedWithDelay(detectionsInFrame, ev, 0);
        for (Lib.Detection detection : detectionsInFrame) {
            verify(mockCallback, times(0)).onNearlyOutOfFrame(detection, DetectionGenerator.getNearlyOutOfFrameSide(ev.getNearlyOutOfFrameThresholds(), detection));
        }
    }

    @Test
    public void testNearlyOutOfFrameWithoutPredecessor() {
        TrackSet ts = TrackSet.getInstance();
        EventDetector ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, ts, table, mockCalc);
        Lib.Detection[] nearlyOutOfFrame;
        for (int i = 0; i < 10; i++) {
            nearlyOutOfFrame = DetectionGenerator.makeNearlyOutOfFrameDetections(ev.getNearlyOutOfFrameThresholds(), SOME_WIDTH, SOME_HEIGHT);
            for (Lib.Detection detection : nearlyOutOfFrame) {
                invokeOnObjectDetectedWithDelay(new Lib.Detection[]{detection}, ev, i);
                verify(mockCallback, times(0)).onNearlyOutOfFrame(detection, DetectionGenerator.getNearlyOutOfFrameSide(ev.getNearlyOutOfFrameThresholds(), detection));
                ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, ts, table, mockCalc);
            }
        }
    }

    @Test
    public void testDetectionsGoingIntoNet() {
        EventDetector ev = new EventDetector(mockConfig, SOME_WIDTH, SOME_HEIGHT, TrackSet.getInstance(), table, mockCalc);
        Lib.Detection[] detections = generateDetectionsGoingIntoNet();
        invokeOnObjectDetectedWithDelay(new Lib.Detection[]{detections[0]}, ev, 0);
        verify(mockCallback, never()).onBallMovingIntoNet();

        // now it should dispatch an event as there are now 2 detections moving into the net
        invokeOnObjectDetectedWithDelay(new Lib.Detection[]{detections[1]}, ev, 0);
        verify(mockCallback, times(1)).onBallMovingIntoNet();

        // going further: it should not dispatch another event until SideChange is called
        invokeOnObjectDetectedWithDelay(new Lib.Detection[]{detections[2]}, ev, 0);
        verify(mockCallback, times(1)).onBallMovingIntoNet();
    }

    private Lib.Detection[] generateDetectionsGoingIntoNet() {
        Lib.Detection first = new Lib.Detection();
        Lib.Detection second = new Lib.Detection();
        Lib.Detection last = new Lib.Detection();
        first.centerX = 700;                    // detection is left to the net
        first.centerY = 860;                    // detection should be on table
        first.radius = 4.5f;                    // detection should be on table
        first.id = 0;

        second.centerX = 800;                   // detection is left to the net
        second.centerY = 880;                   // detection should be on table
        second.radius = 4.5f;                   // detection should be on table
        second.id = 1;
        second.predecessor = first;
        second.predecessorId = first.id;

        last.centerX = 900;                     // detection is left to the net
        last.centerY = 900;                     // detection should be on table
        last.radius = 4.5f;                     // detection should be on table
        last.id = 2;
        last.predecessor = second;
        last.predecessorId = second.id;
        return new Lib.Detection[]{first, second, last};
    }

    private void invokeOnObjectDetectedWithDelay(Lib.Detection[] allDetections, EventDetector ev, int nStartFramesDelay) {
        int delay = 1000 / FRAME_RATE * 1000 * 1000;
        long detectionTime = System.nanoTime() + nStartFramesDelay * delay;
        for (Lib.Detection detection : allDetections) {
            detectionTime = detectionTime + delay;
            ev.onObjectsDetected(new Lib.Detection[]{detection}, detectionTime);
        }
    }
}