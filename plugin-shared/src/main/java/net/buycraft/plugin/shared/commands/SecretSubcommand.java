package net.buycraft.plugin.shared.commands;

import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.shared.IBuycraftPlugin;

import java.io.IOException;
import java.util.logging.Level;

public class SecretSubcommand implements BuycraftSubcommand {
    @Override
    public void execute(final IBuycraftPlugin plugin, final BuycraftCommandSender player, final String[] args) {
        if (!player.isConsole()) {
            player.sendMessage(ChatColor.RED, "secret_console_only");
            return;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED, "secret_need_key");
            return;
        }

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                ApiClient client = new ProductionApiClient(args[0], plugin.getHttpClient());
                ServerInformation information;
                try {
                    information = client.getServerInformation();
                } catch (Exception e) {
                    plugin.getPlatform().log(Level.SEVERE, "Unable to verify secret", e);
                    player.sendMessage(ChatColor.RED, "secret_does_not_work");
                    return;
                }

                plugin.setApiClient(client);
                plugin.getConfiguration().setServerKey(args[0]);
                plugin.setServerInformation(information);
                try {
                    plugin.saveConfiguration();
                } catch (IOException e) {
                    player.sendMessage(ChatColor.RED, "secret_cant_be_saved");
                }

                player.sendMessage(ChatColor.GREEN, "secret_success",
                        information.getServer().getName(), information.getAccount().getName());

                plugin.getDuePlayerFetcher().run(false);
            }
        });
    }

    @Override
    public String getDescriptionMessageName() {
        return "usage_secret";
    }
}
