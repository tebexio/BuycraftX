package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class BuyCommand implements CommandExecutor {
    private final BuycraftPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "I do not need any parameters!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by players.");
            return true;
        }

        if (plugin.getApiClient() == null) {
            sender.sendMessage(ChatColor.RED + "Set up a secret key first with /buycraft secret.");
            return true;
        }

        plugin.getViewCategoriesGUI().open((Player) sender);
        return true;
    }
}
