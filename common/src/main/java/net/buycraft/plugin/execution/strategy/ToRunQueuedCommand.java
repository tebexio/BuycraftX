package net.buycraft.plugin.execution.strategy;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;

import java.util.concurrent.TimeUnit;

@Value
@EqualsAndHashCode(exclude = {"queueTime"})
public class ToRunQueuedCommand {
    private final QueuedPlayer player;
    private final QueuedCommand command;
    private final boolean requireOnline;
    private final long queueTime = System.currentTimeMillis();

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
}
