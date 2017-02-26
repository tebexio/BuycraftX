package net.buycraft.plugin.nukkit.command;

import cn.nukkit.command.CommandSender;

public interface Subcommand {
    void execute(CommandSender sender, String[] args);

    String getDescription();
}
