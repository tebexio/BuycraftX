package net.buycraft.plugin.bungeecord.command;

import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

public class ForceCheckSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    public ForceCheckSubcommand(final BuycraftPlugin plugin) {
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

        if (plugin.getDuePlayerFetcher().inProgress()) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("already_checking_for_purchases"));
            return;
        }

        plugin.getProxy().getScheduler().runAsync(plugin, () -> plugin.getDuePlayerFetcher().run(false));
        sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("forcecheck_queued"));
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_forcecheck");
    }
}
