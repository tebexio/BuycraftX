package net.buycraft.plugin.shared.commands;

import net.buycraft.plugin.shared.IBuycraftPlugin;

public class ForceCheckSubcommand implements BuycraftSubcommand {
    @Override
    public void execute(final IBuycraftPlugin plugin, BuycraftCommandSender player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED, "no_params");
            return;
        }

        if (plugin.getPlatform().getApiClient() == null) {
            player.sendMessage(ChatColor.RED, "need_secret_key");
            return;
        }

        if (plugin.getDuePlayerFetcher().inProgress()) {
            player.sendMessage(ChatColor.RED, "already_checking_for_purchases");
            return;
        }

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                plugin.getDuePlayerFetcher().run(false);
            }
        });

        player.sendMessage(ChatColor.GREEN, "forcecheck_queued");
    }

    @Override
    public String getDescriptionMessageName() {
        return "usage_forcecheck";
    }
}
