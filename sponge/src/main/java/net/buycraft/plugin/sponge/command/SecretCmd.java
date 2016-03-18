package net.buycraft.plugin.sponge.command;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;

@AllArgsConstructor
public class SecretCmd implements CommandExecutor {

    private final BuycraftPlugin plugin;

    @Override
    public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
        if (!(src instanceof ConsoleSource)) {
            src.sendMessage(
                    Text.builder("For security reasons, your Buycraft secret key must be set via the console.").color(TextColors.RED).build());
        } else {
            if (!args.getOne("secret").isPresent()) {
                src.sendMessage(Text.builder("You must specify your server key. You can find your key at https://server.buycraft.net/servers.")
                        .color(TextColors.RED).build());
            } else {
                plugin.getPlatform().executeAsync(new Runnable() {
                    @Override
                    public void run() {
                        ApiClient client = new ProductionApiClient((String) args.getOne("secret").get(), plugin.getHttpClient());
                        try {
                            plugin.updateInformation(client);
                        } catch (IOException | ApiException e) {
                            src.sendMessage(Text.builder("Apologies, but that key didn't seem to work. Try again.").color(TextColors.RED).build());
                            return;
                        }

                        ServerInformation information = plugin.getServerInformation();
                        plugin.setApiClient(client);
                        plugin.getListingUpdateTask().run();
                        plugin.getConfiguration().setServerKey((String) args.getOne("secret").get());
                        try {
                            plugin.saveConfiguration();
                        } catch (IOException e) {
                            src.sendMessage(Text.builder("Apologies, but we couldn't save the public key to your configuration file.").color(TextColors.RED).build());
                        }
                        src.sendMessage(Text.builder(String.format("Looks like you're good to go! " +
                                        "This server is now registered as server '%s' for the web store '%s'.",
                                information.getServer().getName(), information.getAccount().getName())).color(TextColors.GREEN).build());
                        plugin.getPlatform().executeAsync(plugin.getDuePlayerFetcher());
                    }
                });
            }
        }
        return CommandResult.success();
    }
}
