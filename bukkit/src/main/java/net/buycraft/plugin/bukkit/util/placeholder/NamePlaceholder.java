package net.buycraft.plugin.bukkit.util.placeholder;

import net.buycraft.plugin.data.QueuedCommand;

public class NamePlaceholder implements Placeholder {
    @Override
    public String replace(String command, QueuedCommand queuedCommand) {
        return command.replace("{name}", queuedCommand.getPlayer().getUsername());
    }
}
