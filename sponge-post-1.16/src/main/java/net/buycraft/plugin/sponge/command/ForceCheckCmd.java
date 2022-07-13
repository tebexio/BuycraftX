package net.buycraft.plugin.sponge.command;

import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.util.Color;

public class ForceCheckCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public ForceCheckCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext args) throws CommandException {
        Audience sender = (Audience) args.cause().root();

        if (plugin.getApiClient() == null) {
            sender.sendMessage(Component.text(plugin.getI18n().get("need_secret_key")).color(TextColor.color(Color.RED)));
            return CommandResult.success();
        }

        if (plugin.getDuePlayerFetcher().inProgress()) {
            sender.sendMessage(Component.text(plugin.getI18n().get("already_checking_for_purchases")).color(TextColor.color(Color.RED)));
            return CommandResult.success();
        }

        plugin.getPlatform().executeAsync(() -> plugin.getDuePlayerFetcher().run(false));
        sender.sendMessage(Component.text(plugin.getI18n().get("forcecheck_queued")).color(TextColor.color(Color.GREEN)));
        return CommandResult.success();
    }
}
