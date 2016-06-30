package net.buycraft.plugin.bungeecord;

import lombok.Getter;
import net.buycraft.plugin.bungeecord.command.Subcommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class BuycraftCommand extends Command {
    @Getter
    private final Map<String, Subcommand> subcommandMap = new LinkedHashMap<>();
    private final BuycraftPlugin plugin;

    public BuycraftCommand(BuycraftPlugin plugin) {
        super("buycraft");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("buycraft.admin")) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_permission"));
            return;
        }

        if (args.length == 0) {
            showHelp(sender);
            return;
        }

        for (Map.Entry<String, Subcommand> entry : subcommandMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(args[0])) {
                String[] withoutSubcommand = Arrays.copyOfRange(args, 1, args.length);
                entry.getValue().execute(sender, withoutSubcommand);
                return;
            }
        }

        showHelp(sender);

        return;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + plugin.getI18n().get("usage"));

        for (Map.Entry<String, Subcommand> entry : subcommandMap.entrySet()) {
            sender.sendMessage(ChatColor.GREEN + "/buycraft " + entry.getKey() + ChatColor.GRAY + ": " + entry.getValue().getDescription());
        }
    }
}
