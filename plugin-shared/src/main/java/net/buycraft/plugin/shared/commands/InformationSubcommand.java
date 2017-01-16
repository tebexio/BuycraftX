package net.buycraft.plugin.shared.commands;

import net.buycraft.plugin.shared.IBuycraftPlugin;

public class InformationSubcommand implements BuycraftSubcommand {
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

        if (plugin.getPlatform().getServerInformation() == null) {
            player.sendMessage(ChatColor.RED, "information_no_server");
            return;
        }

        player.sendMessage(ChatColor.GRAY, "information_title");
        player.sendMessage(ChatColor.GRAY, "information_server",
                plugin.getPlatform().getServerInformation().getServer().getName(),
                plugin.getPlatform().getServerInformation().getAccount().getName());
        player.sendMessage(ChatColor.GRAY, "information_currency",
                plugin.getPlatform().getServerInformation().getAccount().getCurrency().getIso4217());
        player.sendMessage(ChatColor.GRAY, "information_domain",
                plugin.getPlatform().getServerInformation().getAccount().getDomain());
    }

    @Override
    public String getDescriptionMessageName() {
        return "usage_information";
    }
}
