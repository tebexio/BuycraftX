package net.buycraft.plugin.execution.strategy;

public interface CommandExecutor {
    void queue(ToRunQueuedCommand command);
}
