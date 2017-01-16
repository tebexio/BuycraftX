package net.buycraft.plugin.shared.commands;

import net.buycraft.plugin.shared.IBuycraftPlugin;

public class RefreshSubcommand implements BuycraftSubcommand {
    @Override
    public void execute(IBuycraftPlugin plugin, BuycraftCommandSender player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED, "no_params");
            return;
        }

        if (plugin.getPlatform().getApiClient() == null) {
            player.sendMessage(ChatColor.RED, "need_secret_key");
            return;
        }

        plugin.getPlatform().executeAsync(plugin.getListingUpdateTask());
        player.sendMessage(ChatColor.GREEN, "refresh_queued");
    }

    @Override
    public String getDescriptionMessageName() {
        return "usage_refresh";
    }
}
