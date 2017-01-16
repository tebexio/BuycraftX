package net.buycraft.plugin.bungeecord.command;

import net.buycraft.plugin.bungeecord.BungeeCordBuycraftCommandSender;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.shared.IBuycraftPlugin;
import net.buycraft.plugin.shared.commands.BuycraftCommandSender;
import net.buycraft.plugin.shared.commands.BuycraftSubcommand;
import net.buycraft.plugin.shared.commands.ChatColor;
import net.md_5.bungee.api.ProxyServer;

import java.io.IOException;
import java.util.logging.Level;

public class SecretSubcommand implements BuycraftSubcommand {
    @Override
    public void execute(final IBuycraftPlugin plugin, final BuycraftCommandSender player, final String[] args) {
        if (((BungeeCordBuycraftCommandSender) player).getBungeeSender() == ProxyServer.getInstance().getConsole()) { // rather cross-platform
            player.sendMessage(ChatColor.RED, "secret_console_only");
            return;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED, "secret_need_key");
            return;
        }

        final BuycraftPlugin bungeePlugin = (BuycraftPlugin) plugin;
        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                ApiClient client = new ProductionApiClient(args[0], plugin.getHttpClient());
                try {
                    bungeePlugin.updateInformation(client);
                } catch (Exception e) {
                    bungeePlugin.getLogger().log(Level.SEVERE, "Unable to verify secret", e);
                    player.sendMessage(ChatColor.RED, "secret_does_not_work");
                    return;
                }

                ServerInformation information = plugin.getPlatform().getServerInformation();
                bungeePlugin.setApiClient(client);
                plugin.getConfiguration().setServerKey(args[0]);
                try {
                    bungeePlugin.saveConfiguration();
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
