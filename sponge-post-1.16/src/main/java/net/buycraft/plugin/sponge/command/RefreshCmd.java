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

public class RefreshCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public RefreshCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext args) throws CommandException {
        Audience src = (Audience) args.cause().root();

        if (plugin.getApiClient() == null) {
            src.sendMessage(Component.text(plugin.getI18n().get("need_secret_key")).color(TextColor.color(Color.RED)));
        } else {
            plugin.getPlatform().executeAsync(plugin.getListingUpdateTask());
            src.sendMessage(Component.text(plugin.getI18n().get("refresh_queued")).color(TextColor.color(Color.GREEN)));
        }
        return CommandResult.success();
    }
}
