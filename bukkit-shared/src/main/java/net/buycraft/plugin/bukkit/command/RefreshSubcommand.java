package net.buycraft.plugin.bukkit.command;

import net.buycraft.plugin.bukkit.BuycraftPluginBase;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RefreshSubcommand implements Subcommand {
    private final BuycraftPluginBase plugin;

    public RefreshSubcommand(final BuycraftPluginBase plugin) {
        this.plugin = plugin;
    }

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
