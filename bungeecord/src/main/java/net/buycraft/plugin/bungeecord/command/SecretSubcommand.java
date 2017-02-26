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
import java.util.logging.Level;

@RequiredArgsConstructor
public class SecretSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.equals(plugin.getProxy().getConsole())) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("secret_console_only"));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("secret_need_key"));
            return;
        }

        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                ApiClient client = new ProductionApiClient(args[0], plugin.getHttpClient());
                try {
                    plugin.updateInformation(client);
                } catch (IOException | ApiException e) {
                    plugin.getLogger().log(Level.SEVERE, "Unable to verify secret", e);
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("secret_does_not_work"));
                    return;
                }

                ServerInformation information = plugin.getServerInformation();
                plugin.setApiClient(client);
                plugin.getConfiguration().setServerKey(args[0]);
                plugin.getCouponUpdateTask().run();
                try {
                    plugin.saveConfiguration();
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("secret_cant_be_saved"));
                }

                sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("secret_success",
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
