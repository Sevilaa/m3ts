package ch.m3ts.eventbus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.m3ts.detection.EventDetectionListener;
import ch.m3ts.eventbus.event.ball.BallBounceAudioData;
import ch.m3ts.eventbus.event.ball.BallBounceData;
import ch.m3ts.eventbus.event.ball.BallDroppedSideWaysData;
import ch.m3ts.eventbus.event.ball.BallMovingIntoNetData;
import ch.m3ts.eventbus.event.ball.BallNearlyOutOfFrameData;
import ch.m3ts.eventbus.event.ball.BallTrackData;
import ch.m3ts.eventbus.event.ball.DetectionTimeOutData;
import ch.m3ts.eventbus.event.ball.EventDetectorEventData;
import ch.m3ts.eventbus.event.ball.StrikerSideChangeData;
import ch.m3ts.eventbus.event.ball.TableSideChangeData;
import ch.m3ts.eventbus.event.game.GameEventData;
import ch.m3ts.eventbus.event.game.GameWinData;
import ch.m3ts.eventbus.event.game.GameWinResetData;
import ch.m3ts.eventbus.event.scoremanipulation.PauseMatch;
import ch.m3ts.eventbus.event.scoremanipulation.PointAddition;
import ch.m3ts.eventbus.event.scoremanipulation.PointDeduction;
import ch.m3ts.eventbus.event.scoremanipulation.ResumeMatch;
import ch.m3ts.eventbus.event.scoremanipulation.ScoreManipulationData;
import ch.m3ts.eventbus.event.todisplay.InvalidServeData;
import ch.m3ts.eventbus.event.todisplay.MatchEndedData;
import ch.m3ts.eventbus.event.todisplay.ReadyToServeData;
import ch.m3ts.eventbus.event.todisplay.ScoreData;
import ch.m3ts.eventbus.event.todisplay.ToDisplayData;
import ch.m3ts.eventbus.event.todisplay.ToDisplayGameWinData;
import ch.m3ts.tabletennis.match.DisplayUpdateListener;
import ch.m3ts.tabletennis.match.GameListener;
import ch.m3ts.tabletennis.match.game.ScoreManipulationListener;
import ch.m3ts.util.Side;
import cz.fmo.Lib;
import cz.fmo.data.Track;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class StubListenerAll implements Subscribable {
    private final DisplayUpdateListener displayUpdateListener;
    private final ScoreManipulationListener scoreManipulationListener;
    private final GameListener gameListener;
    private final EventDetectionListener eventDetectionListener;

    StubListenerAll(DisplayUpdateListener displayUpdateListener, ScoreManipulationListener scoreManipulationListener,
                    GameListener gameListener, EventDetectionListener eventDetectionListener) {
        this.displayUpdateListener = displayUpdateListener;
        this.scoreManipulationListener = scoreManipulationListener;
        this.gameListener = gameListener;
        this.eventDetectionListener = eventDetectionListener;
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof ToDisplayData) {
            ToDisplayData toDisplayData = (ToDisplayData) data;
            toDisplayData.call(displayUpdateListener);
        } else if (data instanceof ScoreManipulationData) {
            ScoreManipulationData scoreManipulationData = (ScoreManipulationData) data;
            scoreManipulationData.call(scoreManipulationListener);
        } else if (data instanceof GameEventData) {
            GameEventData gameEventData = (GameEventData) data;
            gameEventData.call(gameListener);
        } else if (data instanceof EventDetectorEventData) {
            EventDetectorEventData eventDetectorEventData = (EventDetectorEventData) data;
            eventDetectorEventData.call(eventDetectionListener);
        }
    }
}

public class TTEventBusTest {
    private StubListenerAll stubListenerAll;
    private DisplayUpdateListener displayUpdateListener;
    private ScoreManipulationListener scoreManipulationListener;
    private GameListener gameListener;
    private EventDetectionListener eventDetectionListener;
    private EventBus eventBus;
    private final static String WINNER_NAME = "some_winner_name_dude";

    @Before
    public void setup() {
        eventBus = TTEventBus.getInstance();
        displayUpdateListener = Mockito.mock(DisplayUpdateListener.class);
        scoreManipulationListener = Mockito.mock(ScoreManipulationListener.class);
        gameListener = Mockito.mock(GameListener.class);
        eventDetectionListener = Mockito.mock(EventDetectionListener.class);
        stubListenerAll = new StubListenerAll(displayUpdateListener, scoreManipulationListener,
                gameListener, eventDetectionListener);
        eventBus.register(stubListenerAll);
    }

    @After
    public void cleanup() {
        eventBus.unregister(stubListenerAll);
    }

    @Test
    public void getInstance() {
        assertEquals(eventBus, TTEventBus.getInstance());
        assertEquals(eventBus, TTEventBus.getInstance());
        assertEquals(eventBus, TTEventBus.getInstance());
    }

    @Test
    public void registerAndUnregister() {
        Subscribable subscribable = mock(Subscribable.class);
        eventBus.dispatch(new TTEvent<>(new InvalidServeData()));
        verify(subscribable, times(0)).handle(any(TTEvent.class));
        eventBus.register(subscribable);
        eventBus.dispatch(new TTEvent<>(new InvalidServeData()));
        verify(subscribable, times(1)).handle(any(TTEvent.class));
        eventBus.unregister(subscribable);
        eventBus.dispatch(new TTEvent<>(new InvalidServeData()));
        verify(subscribable, times(1)).handle(any(TTEvent.class));
    }

    @Test
    public void dispatchToDisplayEvents() {
        eventBus.dispatch(new TTEvent<>(new InvalidServeData()));
        eventBus.dispatch(new TTEvent<>(new MatchEndedData(WINNER_NAME)));
        eventBus.dispatch(new TTEvent<>(new ReadyToServeData(Side.LEFT)));
        eventBus.dispatch(new TTEvent<>(new ScoreData(Side.LEFT, 4, Side.RIGHT, Side.LEFT)));
        eventBus.dispatch(new TTEvent<>(new ToDisplayGameWinData(Side.LEFT, 2)));
        Mockito.verify(displayUpdateListener, times(1)).onNotReadyButPlaying();
        Mockito.verify(displayUpdateListener, times(1)).onMatchEnded(WINNER_NAME);
        Mockito.verify(displayUpdateListener, times(1)).onReadyToServe(Side.LEFT);
        Mockito.verify(displayUpdateListener, times(1)).onScore(Side.LEFT, 4, Side.RIGHT, Side.LEFT);
        Mockito.verify(displayUpdateListener, times(1)).onWin(Side.LEFT, 2);
    }

    @Test
    public void dispatchScoreManipulationEvents() {
        eventBus.dispatch(new TTEvent<>(new PauseMatch()));
        eventBus.dispatch(new TTEvent<>(new PointAddition(Side.LEFT)));
        eventBus.dispatch(new TTEvent<>(new PointDeduction(Side.RIGHT)));
        eventBus.dispatch(new TTEvent<>(new ResumeMatch()));
        Mockito.verify(scoreManipulationListener, times(1)).onPause();
        Mockito.verify(scoreManipulationListener, times(1)).onPointAddition(Side.LEFT);
        Mockito.verify(scoreManipulationListener, times(1)).onPointDeduction(Side.RIGHT);
        Mockito.verify(scoreManipulationListener, times(1)).onResume();
    }

    @Test
    public void dispatchGameEvents() {
        eventBus.dispatch(new TTEvent<>(new GameWinData(Side.LEFT)));
        eventBus.dispatch(new TTEvent<>(new GameWinResetData()));
        Mockito.verify(gameListener, times(1)).onGameWin(Side.LEFT);
        Mockito.verify(gameListener, times(1)).onGameWinReset();
    }

    @Test
    public void dispatchEventDetectorEvents() {
        Lib.Detection detection = mock(Lib.Detection.class);
        Track track = mock(Track.class);
        eventBus.dispatch(new TTEvent<>(new BallBounceAudioData(Side.LEFT)));
        eventBus.dispatch(new TTEvent<>(new BallBounceData(detection, Side.RIGHT)));
        eventBus.dispatch(new TTEvent<>(new BallDroppedSideWaysData()));
        eventBus.dispatch(new TTEvent<>(new BallNearlyOutOfFrameData(detection, Side.LEFT)));
        eventBus.dispatch(new TTEvent<>(new BallTrackData(track)));
        eventBus.dispatch(new TTEvent<>(new DetectionTimeOutData()));
        eventBus.dispatch(new TTEvent<>(new StrikerSideChangeData(Side.RIGHT)));
        eventBus.dispatch(new TTEvent<>(new TableSideChangeData(Side.LEFT)));
        eventBus.dispatch(new TTEvent<>(new BallMovingIntoNetData()));
        Mockito.verify(eventDetectionListener, times(1)).onAudioBounce(Side.LEFT);
        Mockito.verify(eventDetectionListener, times(1)).onBounce(detection, Side.RIGHT);
        Mockito.verify(eventDetectionListener, times(1)).onBallDroppedSideWays();
        Mockito.verify(eventDetectionListener, times(1)).onNearlyOutOfFrame(detection, Side.LEFT);
        Mockito.verify(eventDetectionListener, times(1)).onStrikeFound(track);
        Mockito.verify(eventDetectionListener, times(1)).onTimeout();
        Mockito.verify(eventDetectionListener, times(1)).onSideChange(Side.RIGHT);
        Mockito.verify(eventDetectionListener, times(1)).onTableSideChange(Side.LEFT);
        Mockito.verify(eventDetectionListener, times(1)).onBallMovingIntoNet();
    }

}