package net.buycraft.plugin.bukkit.tasks;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import mkremins.fanciful.FancyMessage;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.responses.CheckoutUrlResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;

@RequiredArgsConstructor
public class SendCheckoutLink implements Runnable {
    @NonNull
    private final BuycraftPlugin plugin;
    @NonNull
    private final int pkgId;
    @NonNull
    private final Player player;

    @Override
    public void run() {
        CheckoutUrlResponse response;
        try {
            response = plugin.getApiClient().getCheckoutUri(player.getName(), pkgId);
        } catch (IOException | ApiException e) {
            player.sendMessage(ChatColor.RED + "Whoops! We weren't able to get a link for you to check out this package.");
            return;
        }

        player.sendMessage(ChatColor.STRIKETHROUGH + "                                            ");
        new FancyMessage("To buy your package, click ")
                .color(ChatColor.GRAY)
                .then("here")
                .color(ChatColor.GREEN)
                .style(ChatColor.UNDERLINE)
                .link(response.getUrl())
                .then(".")
                .color(ChatColor.GRAY)
                .send(player);
        player.sendMessage(ChatColor.STRIKETHROUGH + "                                            ");
    }
}
