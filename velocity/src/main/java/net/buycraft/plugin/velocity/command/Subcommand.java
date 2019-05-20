package net.buycraft.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;

public interface Subcommand {
    void execute(CommandSource sender, String[] args);

    String getDescription();
}
