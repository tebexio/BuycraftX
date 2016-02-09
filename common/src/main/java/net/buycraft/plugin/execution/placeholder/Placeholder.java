package net.buycraft.plugin.execution.placeholder;

import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;

public interface Placeholder {
    String replace(String command, QueuedPlayer player, QueuedCommand queuedCommand);
}
