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
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_params"));
            return;
        }

        if (plugin.getApiClient() == null) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("need_secret_key"));
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, plugin.getListingUpdateTask());
        sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("refresh_queued"));
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_refresh");
    }
}
