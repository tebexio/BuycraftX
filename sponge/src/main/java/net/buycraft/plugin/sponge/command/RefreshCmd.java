package net.buycraft.plugin.sponge.command;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by meyerzinn on 2/14/16.
 */
@AllArgsConstructor
public class RefreshCmd implements CommandExecutor {

    private final BuycraftPlugin plugin;

    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (plugin.getApiClient() == null) {
            src.sendMessage(Text.builder("Set up a secret key first with /buycraft secret.").color(TextColors.RED).build());
        } else {
            plugin.getPlatform().executeAsync(plugin.getListingUpdateTask());
            src.sendMessage(Text.builder("Listing update queued.").color(TextColors.GREEN).build());
        }
        return CommandResult.success();
    }
}
