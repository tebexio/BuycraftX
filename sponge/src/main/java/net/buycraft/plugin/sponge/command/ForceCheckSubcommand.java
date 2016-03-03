package net.buycraft.plugin.sponge.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@RequiredArgsConstructor
public class ForceCheckSubcommand implements CommandExecutor {
    private final BuycraftPlugin plugin;

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
        if (plugin.getApiClient() == null) {
            sender.sendMessage(Text.builder("Set up a secret key first with /buycraft secret.").color(TextColors.RED).build());
            return CommandResult.success();
        }

        if (plugin.getDuePlayerFetcher().getInProgress().get()) {
            sender.sendMessage(Text.builder("We're currently checking for new purchases. Sit tight!").color(TextColors.RED).build());
            return CommandResult.success();
        }

        Sponge.getScheduler().createTaskBuilder().execute(() -> plugin.getDuePlayerFetcher().run(false)).async().submit(plugin);

        sender.sendMessage(Text.builder("Successfully queued player check.").color(TextColors.GREEN).build());
        return CommandResult.success();
    }
}
