package io.tebex.plugin.command;

import com.google.common.collect.ImmutableList;
import io.tebex.plugin.manager.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TebexCommand implements TabExecutor {
    private CommandManager commandManager;

    public TebexCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage("§8[Tebex] §7Welcome to Tebex!");
            sender.sendMessage("§8[Tebex] §7This server is running version §fv" + commandManager.getPlatform().getDescription().getVersion() + "§7.");
            return true;
        }

        Map<String, SubCommand> commands = commandManager.getCommands();
        if(! commands.containsKey(args[0].toLowerCase())) {
            sender.sendMessage("§8[Tebex] §7Unknown command.");
            return true;
        }

        final SubCommand subCommand = commands.get(args[0].toLowerCase());
        if (! sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage("§b[Tebex] §7You do not have access to that command.");
            return true;
        }

        subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
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
