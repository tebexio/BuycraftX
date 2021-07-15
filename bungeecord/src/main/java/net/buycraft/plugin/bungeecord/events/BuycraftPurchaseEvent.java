package net.buycraft.plugin.bungeecord.events;

import net.buycraft.plugin.execution.strategy.ToRunQueuedCommand;
import net.md_5.bungee.api.plugin.Event;

public class BuycraftPurchaseEvent extends Event {

    private boolean cancelled;
    private final String command;
    private final ToRunQueuedCommand queuedCommand;

    public BuycraftPurchaseEvent(String command, ToRunQueuedCommand queuedCommand) {
        this.command = command;
        this.queuedCommand = queuedCommand;
    }

    public ToRunQueuedCommand getQueuedCommand() {
        return this.queuedCommand;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean status) {
        this.cancelled = status;
    }
}
