package net.buycraft.plugin.nukkit.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import net.buycraft.plugin.nukkit.BuycraftPlugin;

public class InformationSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    public InformationSubcommand(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(TextFormat.RED + plugin.getI18n().get("no_params"));
            return;
        }

        if (plugin.getApiClient() == null) {
            sender.sendMessage(TextFormat.RED + plugin.getI18n().get("need_secret_key"));
            return;
        }

        if (plugin.getServerInformation() == null) {
            sender.sendMessage(TextFormat.RED + plugin.getI18n().get("information_no_server"));
            return;
        }

        sender.sendMessage(TextFormat.GRAY + plugin.getI18n().get("information_title"));
        sender.sendMessage(TextFormat.GRAY + plugin.getI18n().get("information_server",
                plugin.getServerInformation().getServer().getName(),
                plugin.getServerInformation().getAccount().getName()));
        sender.sendMessage(TextFormat.GRAY + plugin.getI18n().get("information_currency",
                plugin.getServerInformation().getAccount().getCurrency().getIso4217()));
        sender.sendMessage(TextFormat.GRAY + plugin.getI18n().get("information_domain",
                plugin.getServerInformation().getAccount().getDomain()));
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_information");
    }
}
