package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class RefreshSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "I do not need any parameters!");
            return;
        }

        if (plugin.getApiClient() == null) {
            sender.sendMessage(ChatColor.RED + "Set up a secret key first with /buycraft secret.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, plugin.getListingUpdateTask());
        sender.sendMessage(ChatColor.GREEN + "Listing refresh queued.");
    }

    @Override
    public String getDescription() {
        return "Refreshes the list of categories and packages.";
    }
}
