package io.tebex.plugin.command.sub;

import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.plugin.manager.CommandManager;
import net.md_5.bungee.api.CommandSender;

import java.util.Comparator;

public class HelpCommand extends SubCommand {
    private final CommandManager commandManager;

    public HelpCommand(TebexPlugin platform, CommandManager commandManager) {
        super(platform, "help", "tebex.admin");
        this.commandManager = commandManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("§b[Tebex] §7Plugin Commands:");

        commandManager
                .getCommands()
                .values()
                .stream()
                .sorted(Comparator.comparing(SubCommand::getName))
                .forEach(subCommand -> sender.sendMessage(" §8- §f/tebex " + subCommand.getName() + "§f" + (!subCommand.getUsage().isEmpty() ? " §3" + subCommand.getUsage() + " " : " ") + "§7§o(" + subCommand.getDescription() + ")"));
    }

    @Override
    public String getDescription() {
        return "Shows this help page.";
    }
}
