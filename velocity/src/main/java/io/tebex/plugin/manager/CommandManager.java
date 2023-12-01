package io.tebex.plugin.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.plugin.command.TebexCommand;
import io.tebex.plugin.command.sub.HelpCommand;
import io.tebex.plugin.command.sub.RecheckCommand;
import io.tebex.plugin.command.sub.ReloadCommand;
import io.tebex.plugin.command.sub.SecretCommand;

import java.util.Map;

public class CommandManager {
    private final TebexPlugin platform;
    private final Map<String, SubCommand> commands;

    public CommandManager(TebexPlugin platform) {
        this.platform = platform;
        this.commands = Maps.newHashMap();
    }

    public void register() {
        ImmutableList.of(
                new SecretCommand(platform),
                new ReloadCommand(platform),
                new RecheckCommand(platform),
                new HelpCommand(platform, this)
        ).forEach(command -> {
            commands.put(command.getName(), command);
        });

        SimpleCommand tebexCommand = new TebexCommand(this);

        com.velocitypowered.api.command.CommandManager commandManager = platform.getProxy().getCommandManager();

        CommandMeta commandMeta = commandManager.metaBuilder("tebex")
                .aliases("tbx", "buycraft")
                .plugin(platform)
                .build();

        platform.getProxy().getCommandManager().register(commandMeta, tebexCommand);
    }

    public Map<String, SubCommand> getCommands() {
        return commands;
    }

    public TebexPlugin getPlatform() {
        return platform;
    }
}