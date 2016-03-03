package net.buycraft.plugin.sponge.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;

@RequiredArgsConstructor
public class SecretSubcommand implements CommandExecutor {
    private final BuycraftPlugin plugin;

    @Override
    public CommandResult execute(final CommandSource sender, final CommandContext args) throws CommandException {
        if (!(sender instanceof ConsoleSource)) {
            sender.sendMessage(Text.builder("For security reasons, your Buycraft secret key must be set via the console.").color(TextColors.RED).build());
            return CommandResult.success();
        }

        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            String key = args.<String>getOne("key").get();

            ApiClient client = new ProductionApiClient(key, plugin.getHttpClient());
            try {
                plugin.updateInformation(client);
            } catch (IOException | ApiException e) {
                sender.sendMessage(Text.builder("Apologies, but that key didn't seem to work. Try again.").color(TextColors.RED).build());
                return;
            }

            ServerInformation information = plugin.getServerInformation();
            plugin.setApiClient(client);
            plugin.getListingUpdateTask().run();
            plugin.getConfiguration().setServerKey(key);
            try {
                plugin.saveConfiguration();
            } catch (IOException e) {
                sender.sendMessage(Text.builder("Apologies, but we couldn't save the public key to your configuration file.").color(TextColors.RED).build());
            }

            sender.sendMessage(Text.builder(
                    String.format(
                            "Looks like you're good to go! " +
                                    "This server is now registered as server '%s' for the web store '%s'.",
                            information.getServer().getName(),
                            information.getAccount().getName()
                    )
            ).color(TextColors.GREEN).build());

            Sponge.getScheduler().createAsyncExecutor(plugin).execute(plugin.getDuePlayerFetcher());
        });

        return CommandResult.success();
    }
}
