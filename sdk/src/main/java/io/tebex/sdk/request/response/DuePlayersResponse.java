package io.tebex.sdk.request.response;

import io.tebex.sdk.obj.QueuedPlayer;

import java.util.List;

public class DuePlayersResponse {
    private final boolean executeOffline;
    private final int nextCheck;
    private final boolean more;
    private final List<QueuedPlayer> players;

    /**
     * Constructs a DuePlayersResponse instance.
     *
     * @param executeOffline Can execute offline.
     * @param nextCheck The next check time in seconds.
     * @param more If there are more players.
     * @param players The list of players.
     */
    public DuePlayersResponse(boolean executeOffline, int nextCheck, boolean more, List<QueuedPlayer> players) {
        this.executeOffline = executeOffline;
        this.nextCheck = nextCheck;
        this.more = more;
        this.players = players;
    }

    public boolean canExecuteOffline() {
        return executeOffline;
    }

    public int getNextCheck() {
        return nextCheck;
    }

    public boolean isMore() {
        return more;
    }

    public List<QueuedPlayer> getPlayers() {
        return players;
    }
}