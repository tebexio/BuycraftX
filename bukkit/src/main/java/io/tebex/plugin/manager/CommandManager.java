package io.tebex.plugin.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.plugin.command.TebexCommand;
import io.tebex.plugin.command.sub.HelpCommand;
import io.tebex.plugin.command.sub.RecheckCommand;
import io.tebex.plugin.command.sub.ReloadCommand;
import io.tebex.plugin.command.sub.SecretCommand;
import io.tebex.plugin.command.BuyCommand;
import org.bukkit.command.PluginCommand;

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

        TebexCommand tebexCommand = new TebexCommand(this);
        PluginCommand pluginCommand = platform.getCommand("tebex");

        if(pluginCommand == null) {
            throw new RuntimeException("Tebex command not found.");
        }

        pluginCommand.setExecutor(tebexCommand);
        pluginCommand.setTabCompleter(tebexCommand);
    }

    public Map<String, SubCommand> getCommands() {
        return commands;
    }

    public TebexPlugin getPlatform() {
        return platform;
    }
}
