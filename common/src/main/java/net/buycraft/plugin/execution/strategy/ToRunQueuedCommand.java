package net.buycraft.plugin.execution.strategy;

import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;

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
    public java.lang.String toString() {
        return "ToRunQueuedCommand(player=" + this.getPlayer() + ", command=" + this.getCommand() + ", requireOnline=" + this.isRequireOnline() + ", queueTime=" + this.getQueueTime() + ")";
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ToRunQueuedCommand)) return false;
        final ToRunQueuedCommand other = (ToRunQueuedCommand) o;
        final java.lang.Object this$player = this.getPlayer();
        final java.lang.Object other$player = other.getPlayer();
        if (this$player == null ? other$player != null : !this$player.equals(other$player)) return false;
        final java.lang.Object this$command = this.getCommand();
        final java.lang.Object other$command = other.getCommand();
        if (this$command == null ? other$command != null : !this$command.equals(other$command)) return false;
        if (this.isRequireOnline() != other.isRequireOnline()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $player = this.getPlayer();
        result = result * PRIME + ($player == null ? 43 : $player.hashCode());
        final java.lang.Object $command = this.getCommand();
        result = result * PRIME + ($command == null ? 43 : $command.hashCode());
        result = result * PRIME + (this.isRequireOnline() ? 79 : 97);
        return result;
    }
}
