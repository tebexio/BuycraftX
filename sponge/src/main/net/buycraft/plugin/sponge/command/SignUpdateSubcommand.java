package net.buycraft.plugin.sponge.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@RequiredArgsConstructor
public class SignUpdateSubcommand implements CommandExecutor {
    private final BuycraftPlugin plugin;

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
        sender.sendMessage(Text.builder("This command is Under Development!!!").color(TextColors.RED).build());
        return CommandResult.success();

//        if (plugin.getApiClient() == null) {
//            sender.sendMessage(Text.builder("Set up a secret key first with /buycraft secret.").color(TextColors.RED).build());
//            return CommandResult.success();
//        }
//
//        if (plugin.getDuePlayerFetcher().getInProgress().get()) {
//            sender.sendMessage(Text.builder("We're currently checking for new purchases. Sit tight!").color(TextColors.RED).build());
//            return CommandResult.success();
//        }
    }
}
