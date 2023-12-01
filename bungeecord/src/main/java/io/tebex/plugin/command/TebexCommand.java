package io.tebex.plugin.command;

import com.google.common.collect.ImmutableList;
import io.tebex.plugin.manager.CommandManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class TebexCommand extends Command {
    private CommandManager commandManager;

    public TebexCommand(CommandManager commandManager, String name) {
        super(name);
        this.commandManager = commandManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage("§8[Tebex] §7Welcome to Tebex!");
            sender.sendMessage("§8[Tebex] §7This server is running version §fv" + commandManager.getPlatform().getDescription().getVersion() + "§7.");
            return;
        }

        Map<String, SubCommand> commands = commandManager.getCommands();
        if(! commands.containsKey(args[0].toLowerCase())) {
            sender.sendMessage("§8[Tebex] §7Unknown command.");
            return;
        }

        final SubCommand subCommand = commands.get(args[0].toLowerCase());
        if (! sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage("§b[Tebex] §7You do not have access to that command.");
            return;
        }

        subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1) {
            return commandManager.getCommands()
                    .keySet()
                    .stream()
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }

        return ImmutableList.of();
    }
}
