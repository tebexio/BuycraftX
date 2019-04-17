package net.buycraft.plugin.sponge.command;

import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class RefreshCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public RefreshCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (plugin.getApiClient() == null) {
            src.sendMessage(Text.builder(plugin.getI18n().get("need_secret_key")).color(TextColors.RED).build());
        } else {
            plugin.getPlatform().executeAsync(plugin.getListingUpdateTask());
            src.sendMessage(Text.builder(plugin.getI18n().get("refresh_queued")).color(TextColors.GREEN).build());
        }
        return CommandResult.success();
    }
}
