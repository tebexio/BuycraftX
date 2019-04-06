package net.buycraft.plugin.execution.strategy;

import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class ToRunQueuedCommand {
    private final QueuedPlayer player;
    private final QueuedCommand command;
    private final boolean requireOnline;
    private final long queueTime = System.currentTimeMillis();

    public ToRunQueuedCommand(final QueuedPlayer player, final QueuedCommand command, final boolean requireOnline) {
        this.player = player;
        this.command = command;
        this.requireOnline = requireOnline;
    }

    public boolean canExecute(IBuycraftPlatform platform) {
        Integer requiredSlots = command.getConditions().get("slots");

        if (requiredSlots != null || requireOnline) {
            if (!platform.isPlayerOnline(player)) {
                return false;
            }
        }

        if (requiredSlots != null) {
            int free = platform.getFreeSlots(player);
            if (free < requiredSlots) {
                return false;
            }
        }

        Integer delay = command.getConditions().get("delay");
        return !(delay != null && delay > 0 && System.currentTimeMillis() - queueTime < TimeUnit.SECONDS.toMillis(delay));
    }

    public QueuedPlayer getPlayer() {
        return this.player;
    }

    public QueuedCommand getCommand() {
        return this.command;
    }

    public boolean isRequireOnline() {
        return this.requireOnline;
    }

    public long getQueueTime() {
        return this.queueTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToRunQueuedCommand that = (ToRunQueuedCommand) o;

        if (requireOnline != that.requireOnline) return false;
        if (!Objects.equals(player, that.player)) return false;
        return Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        int result = player != null ? player.hashCode() : 0;
        result = 31 * result + (command != null ? command.hashCode() : 0);
        result = 31 * result + (requireOnline ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ToRunQueuedCommand(player=" + this.getPlayer() + ", command=" + this.getCommand() + ", requireOnline=" + this.isRequireOnline() + ", queueTime=" + this.getQueueTime() + ")";
    }
}
