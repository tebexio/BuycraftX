package net.buycraft.plugin.sponge.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;

@RequiredArgsConstructor
public class InformationSubcommand implements CommandExecutor {
    private final BuycraftPlugin plugin;

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
        if (plugin.getApiClient() == null) {
            sender.sendMessage(Text.builder("Set up a secret key first with /buycraft secret.").color(TextColors.RED).build());
            return CommandResult.success();
        }

        if (plugin.getServerInformation() == null) {
            sender.sendMessage(Text.builder("No server information found.").color(TextColors.RED).build());
            return CommandResult.success();
        }

        String webstoreURL = plugin.getServerInformation().getAccount().getDomain();

        try {
            LiteralText webstore = Text.builder(plugin.getServerInformation().getAccount().getName())
                    .color(TextColors.GREEN)
                    .onClick(TextActions.openUrl(new URL(webstoreURL)))
                    .onHover(TextActions.showText(Text.of(webstoreURL)))
                    .build();

            LiteralText server = Text.builder(plugin.getServerInformation().getServer().getName())
                    .color(TextColors.GREEN)
                    .build();

            LiteralText prices = Text.builder(plugin.getServerInformation().getAccount().getCurrency().getIso4217())
                    .color(TextColors.GREEN)
                    .build();

            sender.sendMessages(
                    Text.builder("Information on this server:").color(TextColors.GRAY).build(),
                    Text.builder("Webstore: ").color(TextColors.GRAY).append(webstore).build(),
                    Text.builder("Server: ").color(TextColors.GRAY).append(server).build(),
                    Text.builder("Server prices are in ").color(TextColors.GRAY).append(prices).build()
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return CommandResult.success();
    }
}
