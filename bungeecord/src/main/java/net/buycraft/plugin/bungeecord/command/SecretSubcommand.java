package net.buycraft.plugin.bungeecord.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import java.io.IOException;

@RequiredArgsConstructor
public class SecretSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.equals(plugin.getProxy().getConsole())) {
            sender.sendMessage(ChatColor.RED + "For security reasons, your Buycraft secret key must be set via the console.");
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "You must specify your server key. You can find your key at https://server.buycraft.net/servers.");
            return;
        }

        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                ApiClient client = new ProductionApiClient(args[0], plugin.getHttpClient());
                try {
                    plugin.updateInformation(client);
                } catch (IOException | ApiException e) {
                    sender.sendMessage(ChatColor.RED + "Apologies, but that key didn't seem to work. Try again.");
                    return;
                }

                ServerInformation information = plugin.getServerInformation();
                plugin.setApiClient(client);
                plugin.getConfiguration().setServerKey(args[0]);
                try {
                    plugin.saveConfiguration();
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "Apologies, but we couldn't save the public key to your configuration file.");
                }

                sender.sendMessage(String.format(ChatColor.GREEN + "Looks like you're good to go! " +
                                "This server is now registered as server '%s' for the web store '%s'.",
                        information.getServer().getName(), information.getAccount().getName()));

                plugin.getProxy().getScheduler().runAsync(plugin, plugin.getDuePlayerFetcher());
            }
        });
    }

    @Override
    public String getDescription() {
        return "Sets the secret key to use for this server.";
    }
}
