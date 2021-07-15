package net.buycraft.plugin.bukkit.events;

import net.buycraft.plugin.execution.strategy.ToRunQueuedCommand;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BuycraftPurchaseEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean status) {
        this.cancelled = status;
    }
}
