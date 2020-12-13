package ch.m3ts.tabletennis.match;

import ch.m3ts.tabletennis.helper.Side;
import ch.m3ts.tabletennis.match.game.GameType;

public class MatchSettings {
    private final MatchType matchType;
    private final Player playerLeft;
    private final Player playerRight;
    private final Side startingServer;
    private final GameType gameType;

    private final ServeRules serveRules;

    public MatchSettings(MatchType matchType, GameType gameType, ServeRules serveRules, Player playerLeft, Player playerRight, Side startingServer) {
        this.matchType = matchType;
        this.playerLeft = playerLeft;
        this.playerRight = playerRight;
        this.startingServer = startingServer;
        this.gameType = gameType;
        this.serveRules = serveRules;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public Player getPlayerLeft() {
        return playerLeft;
    }

    public Player getPlayerRight() {
        return playerRight;
    }

    public Side getStartingServer() {
        return startingServer;
    }

    public GameType getGameType() {
        return gameType;
    }

    public ServeRules getServeRules() {
        return serveRules;
    }
}
