package net.buycraft.plugin.bukkit.tasks;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.data.responses.CheckoutUrlResponse;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;

@RequiredArgsConstructor
public class SendCheckoutLink implements Runnable {
    @NonNull
    private final BuycraftPlugin plugin;
    @NonNull
    private final Package aPackage;
    @NonNull
    private final Player player;

    @Override
    public void run() {
        CheckoutUrlResponse response;
        try {
            response = plugin.getApiClient().getCheckoutUri(player.getName(), aPackage.getId());
        } catch (IOException | ApiException e) {
            player.sendMessage(ChatColor.RED + "Whoops! We weren't able to get a link for you to check out this package.");
            return;
        }

        player.sendMessage(ChatColor.STRIKETHROUGH + "                                    ");
        player.spigot().sendMessage(new ComponentBuilder("To buy your package, click ")
                .color(net.md_5.bungee.api.ChatColor.GRAY)
                .append("here")
                .color(net.md_5.bungee.api.ChatColor.AQUA)
                .bold(true)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, response.getUrl()))
                .create());
        player.sendMessage(ChatColor.GRAY +          "This link expires in 15 minutes.");
        player.sendMessage(ChatColor.STRIKETHROUGH + "                                    ");
    }
}
