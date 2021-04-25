package ch.m3ts.tabletennis.match;

import java.util.EnumMap;
import java.util.Map;

import ch.m3ts.display.stats.StatsCreator;
import ch.m3ts.event.Event;
import ch.m3ts.event.Subscribable;
import ch.m3ts.event.TTEvent;
import ch.m3ts.event.TTEventBus;
import ch.m3ts.event.data.StatusUpdateData;
import ch.m3ts.event.data.game.GameEventData;
import ch.m3ts.event.data.scoremanipulation.PointDeduction;
import ch.m3ts.event.data.todisplay.MatchEndedData;
import ch.m3ts.event.data.todisplay.ToDisplayGameWinData;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.game.Game;
import ch.m3ts.tabletennis.match.game.GameType;
import ch.m3ts.tabletennis.match.referee.Referee;


/**
 * Represents a match of table tennis.
 * Notifies when a game or a match is over and provides information about the current match status.
 */
public class Match implements GameListener, MatchStatusCallback, Subscribable {
    private Game[] games;
    private final MatchType type;
    private final Map<Side, Player> players;
    private Map<Side, Integer> wins;
    private final Referee referee;
    private Side serverSide;
    private final Side startingServer;
    private final ServeRules serveRules;
    private final GameType gameType;

    public Match(MatchType type, GameType gameType, ServeRules serveRules, Player playerLeft, Player playerRight, Side startingServer) {
        this.type = type;
        this.gameType = gameType;
        this.startingServer = startingServer;
        init();
        this.players = new EnumMap<>(Side.class);
        this.players.put(Side.LEFT, playerLeft);
        this.players.put(Side.RIGHT, playerRight);
        this.serveRules = serveRules;
        this.referee = new Referee(startingServer);
        TTEventBus.getInstance().register(referee);
        startNewGame(true);
    }

    public Match(MatchSettings settings) {
        this(settings.getMatchType(), settings.getGameType(), settings.getServeRules(),
                settings.getPlayerLeft(), settings.getPlayerRight(), settings.getStartingServer());
    }

    void startNewGame(boolean firstInit) {
        if (!firstInit) switchServers();
        Game game = new Game(gameType, serveRules, this.serverSide);
        this.games[this.wins.get(Side.RIGHT) + this.wins.get(Side.LEFT)] = game;
        this.referee.setGame(game, firstInit);
    }

    public void end(Side winner) {
        TTEventBus.getInstance().dispatch(new TTEvent<>(new MatchEndedData(this.players.get(winner).getName())));
    }

    @Override
    public void onGameWin(Side side) {
        int win = wins.get(side) + 1;
        if (win > type.gamesNeededToWin) win = type.gamesNeededToWin;
        wins.put(side, win);
        if (isMatchOver(win)) {
            end(side);
        } else {
            TTEventBus.getInstance().dispatch(new TTEvent<>(new ToDisplayGameWinData(side, win)));
            startNewGame(false);
        }
    }

    @Override
    public void onGameWinReset() {
        StatsCreator.getInstance().resetGame();
        int gamesPlayed = this.wins.get(Side.LEFT) + this.wins.get(Side.RIGHT);
        if (gamesPlayed <= 0) return;
        Game lastGame = this.games[gamesPlayed - 1];
        Side lastWinner = lastGame.getScore(Side.LEFT) > lastGame.getScore(Side.RIGHT) ? Side.LEFT : Side.RIGHT;
        if (gamesPlayed == 1) {
            this.wins.put(Side.LEFT, 0);
            this.wins.put(Side.RIGHT, 0);
        } else {
            this.wins.put(lastWinner, this.wins.get(lastWinner) - 1);
        }
        this.referee.setGame(lastGame, gamesPlayed == 1);
        TTEventBus.getInstance().dispatch(new TTEvent<>(new PointDeduction(lastWinner)));
        TTEventBus.getInstance().dispatch(new TTEvent<>(new StatusUpdateData(onRequestMatchStatus())));
    }

    public Referee getReferee() {
        return referee;
    }

    public void restartMatch() {
        init();
        startNewGame(true);
        this.referee.initState();
    }

    private void init() {
        this.wins = new EnumMap<>(Side.class);
        this.wins.put(Side.LEFT, 0);
        this.wins.put(Side.RIGHT, 0);
        this.games = new Game[type.amountOfGames];
        this.serverSide = startingServer;
    }

    private boolean isMatchOver(int wins) {
        return (wins >= this.type.gamesNeededToWin);
    }

    private void switchServers() {
        if (this.serverSide == Side.LEFT) {
            this.serverSide = Side.RIGHT;
        } else {
            this.serverSide = Side.LEFT;
        }
    }

    private Game getCurrentGame() {
        if (this.wins.get(Side.RIGHT) + this.wins.get(Side.LEFT) >= this.games.length) {
            return this.games[this.games.length - 1];
        }
        return this.games[this.wins.get(Side.RIGHT) + this.wins.get(Side.LEFT)];
    }

    @Override
    public MatchStatus onRequestMatchStatus() {
        return new MatchStatus(players.get(Side.LEFT).getName(), players.get(Side.RIGHT).getName(), getCurrentGame().getScore(Side.LEFT),
                getCurrentGame().getScore(Side.RIGHT), wins.get(Side.LEFT), wins.get(Side.RIGHT), getCurrentGame().getServer(), this.type.gamesNeededToWin);
    }

    @Override
    public void handle(Event<?> event) {
        Object data = event.getData();
        if (data instanceof GameEventData) {
            GameEventData gameEventData = (GameEventData) data;
            gameEventData.call(this);
        }
    }
}
