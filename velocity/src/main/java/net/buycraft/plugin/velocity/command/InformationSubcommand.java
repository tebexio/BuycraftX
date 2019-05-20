package net.buycraft.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import net.buycraft.plugin.velocity.BuycraftPlugin;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

public class InformationSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    public InformationSubcommand(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSource sender, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(TextComponent.of(plugin.getI18n().get("no_params")).color(TextColor.RED));
            return;
        }

        if (plugin.getApiClient() == null) {
            sender.sendMessage(TextComponent.of(plugin.getI18n().get("need_secret_key")).color(TextColor.RED));
            return;
        }

        if (plugin.getServerInformation() == null) {
            sender.sendMessage(TextComponent.of(plugin.getI18n().get("information_no_server")).color(TextColor.RED));
            return;
        }

        sender.sendMessage(TextComponent.of(plugin.getI18n().get("information_title")).color(TextColor.GRAY));
        sender.sendMessage(TextComponent.of(plugin.getI18n().get("information_server",
                plugin.getServerInformation().getServer().getName(),
                plugin.getServerInformation().getAccount().getName())).color(TextColor.GRAY));
        sender.sendMessage(TextComponent.of(plugin.getI18n().get("information_currency",
                plugin.getServerInformation().getAccount().getCurrency().getIso4217())).color(TextColor.GRAY));
        sender.sendMessage(TextComponent.of(plugin.getI18n().get("information_domain",
                plugin.getServerInformation().getAccount().getDomain())).color(TextColor.GRAY));
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_information");
    }
}
