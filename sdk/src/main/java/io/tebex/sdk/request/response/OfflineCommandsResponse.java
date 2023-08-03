package io.tebex.sdk.request.response;

import io.tebex.sdk.obj.QueuedCommand;

import java.util.List;

public class OfflineCommandsResponse {
    private final boolean limited;
    private final List<QueuedCommand> commands;

    public OfflineCommandsResponse(boolean limited, List<QueuedCommand> commands) {
        this.limited = limited;
        this.commands = commands;
    }

    public boolean isLimited() {
        return limited;
    }

    public List<QueuedCommand> getCommands() {
        return commands;
    }
}