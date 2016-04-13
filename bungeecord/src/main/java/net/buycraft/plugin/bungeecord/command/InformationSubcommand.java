package net.buycraft.plugin.bungeecord.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

@RequiredArgsConstructor
public class InformationSubcommand implements Subcommand {
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

        if (plugin.getServerInformation() == null) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("information_no_server"));
            return;
        }

        sender.sendMessage(ChatColor.GRAY + plugin.getI18n().get("information_title"));
        sender.sendMessage(ChatColor.GRAY + plugin.getI18n().get("information_server",
                plugin.getServerInformation().getServer().getName(),
                plugin.getServerInformation().getAccount().getName()));
        sender.sendMessage(ChatColor.GRAY + plugin.getI18n().get("information_currency",
                plugin.getServerInformation().getAccount().getCurrency().getIso4217()));
        sender.sendMessage(ChatColor.GRAY + plugin.getI18n().get("information_domain",
                plugin.getServerInformation().getAccount().getDomain()));
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_information");
    }
}
