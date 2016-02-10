package net.buycraft.plugin.bungeecord.command;

import net.md_5.bungee.api.CommandSender;

public interface Subcommand {
    void execute(CommandSender sender, String[] args);

    String getDescription();
}
