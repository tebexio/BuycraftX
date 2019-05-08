package net.buycraft.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import net.buycraft.plugin.velocity.BuycraftPlugin;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

public class ForceCheckSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    public ForceCheckSubcommand(final BuycraftPlugin plugin) {
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

        if (plugin.getDuePlayerFetcher().inProgress()) {
            sender.sendMessage(TextComponent.of(plugin.getI18n().get("already_checking_for_purchases")).color(TextColor.RED));
            return;
        }

        plugin.getPlatform().executeAsync(() -> plugin.getDuePlayerFetcher().run(false));
        sender.sendMessage(TextComponent.of(plugin.getI18n().get("forcecheck_queued")).color(TextColor.GREEN));
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_forcecheck");
    }
}
