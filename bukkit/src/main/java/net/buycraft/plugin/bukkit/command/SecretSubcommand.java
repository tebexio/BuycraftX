package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.data.responses.ServerInformation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

@RequiredArgsConstructor
public class SecretSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "I need your secret key!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                ApiClient client = new ProductionApiClient(args[0]);
                ServerInformation information;
                try {
                    information = client.getServerInformation();
                } catch (IOException | ApiException e) {
                    sender.sendMessage(ChatColor.RED + "Apologies, but that key didn't seem to work. Try again.");
                    return;
                }

                plugin.setApiClient(client);
                sender.sendMessage(String.format(ChatColor.GREEN + "Looks like you're good to go!" +
                        "This server is now registered as server '%s' for the web store '%s'.",
                        information.getServer().getName(), information.getAccount().getName()));
            }
        });
    }
}
