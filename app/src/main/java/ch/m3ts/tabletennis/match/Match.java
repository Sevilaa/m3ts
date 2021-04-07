package ch.m3ts.tabletennis.match;

import java.util.EnumMap;
import java.util.Map;

import ch.m3ts.tabletennis.events.GestureCallback;
import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.game.Game;
import ch.m3ts.tabletennis.match.game.GameType;
import ch.m3ts.tabletennis.match.referee.Referee;


/**
 * Represents a match of table tennis.
 * Notifies when a game or a match is over and provides information about the current match status.
 */
public class Match implements MatchCallback, MatchStatusCallback {
    private Game[] games;
    private final MatchType type;
    private final Map<Side, Player> players;
    private Map<Side, Integer> wins;
    private final UICallback uiCallback;
    private final Referee referee;
    private Side serverSide;
    private final Side startingServer;
    private final ServeRules serveRules;
    private final GameType gameType;

    public Match(MatchType type, GameType gameType, ServeRules serveRules, Player playerLeft, Player playerRight, UICallback uiCallback, Side startingServer, GestureCallback gestureCallback) {
        this.type = type;
        this.gameType = gameType;
        this.startingServer = startingServer;
        init();
        this.players = new EnumMap<>(Side.class);
        this.players.put(Side.LEFT, playerLeft);
        this.players.put(Side.RIGHT, playerRight);
        this.uiCallback = uiCallback;
        this.serveRules = serveRules;
        this.referee = new Referee(startingServer, gestureCallback);
        startNewGame(true);
    }

    public Match(MatchSettings settings, UICallback uiCallback, GestureCallback gestureCallback) {
        this(settings.getMatchType(), settings.getGameType(), settings.getServeRules(),
                settings.getPlayerLeft(), settings.getPlayerRight(), uiCallback, settings.getStartingServer(), gestureCallback);
    }

    void startNewGame(boolean firstInit) {
        if (!firstInit) switchServers();
        Game game = new Game(this, uiCallback, gameType, serveRules, this.serverSide);
        this.games[this.wins.get(Side.RIGHT) + this.wins.get(Side.LEFT)] = game;
        this.referee.setGame(game, firstInit);
    }

    public void end(Side winner) {
        this.uiCallback.onMatchEnded(this.players.get(winner).getName());
    }

    @Override
    public void onWin(Side side) {
        int win = wins.get(side) + 1;
        wins.put(side, win);
        uiCallback.onWin(side, win);
        if (isMatchOver(win)) {
            end(side);
        } else {
            startNewGame(false);
        }
    }

    public Referee getReferee() {
        return referee;
    }

    public void restartMatch() {
        init();
        startNewGame(true);
        this.referee.initWaitingForGesture();
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
}
