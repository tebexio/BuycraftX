package net.buycraft.plugin.bukkit.util.placeholder;

import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;

public interface Placeholder {
    String replace(String command, QueuedPlayer player, QueuedCommand queuedCommand);
}
