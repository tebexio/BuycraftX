package net.buycraft.plugin.bukkit.util.placeholder;

import net.buycraft.plugin.data.QueuedCommand;

public interface Placeholder {
    String replace(String command, QueuedCommand queuedCommand);
}
