package net.buycraft.plugin.shared.commands;

import net.buycraft.plugin.shared.IBuycraftPlugin;

public interface BuycraftSubcommand {
    void execute(IBuycraftPlugin plugin, BuycraftCommandSender player, String[] args);

    String getDescriptionMessageName();
}
