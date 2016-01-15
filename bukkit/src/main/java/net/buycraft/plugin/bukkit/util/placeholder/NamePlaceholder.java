package net.buycraft.plugin.bukkit.util.placeholder;

import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;

public class NamePlaceholder implements Placeholder {
    @Override
    public String replace(String command, QueuedPlayer player, QueuedCommand queuedCommand) {
        return command.replace("{name}", player.getName());
    }
}
