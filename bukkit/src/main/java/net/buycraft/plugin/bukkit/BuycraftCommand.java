package net.buycraft.plugin.bukkit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.util.BukkitBuycraftCommandSender;
import net.buycraft.plugin.shared.commands.BuycraftSubcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class BuycraftCommand implements CommandExecutor {
    @Getter
    private final Map<String, BuycraftSubcommand> subcommandMap = new LinkedHashMap<>();
    private final BuycraftPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("buycraft.admin")) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_permission"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        for (Map.Entry<String, BuycraftSubcommand> entry : subcommandMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(args[0])) {
                String[] withoutSubcommand = Arrays.copyOfRange(args, 1, args.length);
                entry.getValue().execute(plugin, new BukkitBuycraftCommandSender(sender, plugin), withoutSubcommand);
                return true;
            }
        }

        showHelp(sender);

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + plugin.getI18n().get("usage"));

        for (Map.Entry<String, BuycraftSubcommand> entry : subcommandMap.entrySet()) {
            sender.sendMessage(ChatColor.GREEN + "/buycraft " + entry.getKey() + ChatColor.GRAY + ": " +
                    plugin.getI18n().get(entry.getValue().getDescriptionMessageName()));
        }
    }
}
