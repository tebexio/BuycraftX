package net.buycraft.plugin.bukkit.tasks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.responses.CheckoutUrlResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.inventivetalent.chat.ChatAPI;

import java.io.IOException;

@RequiredArgsConstructor
public class SendCheckoutLink implements Runnable {
    @NonNull
    private final BuycraftPlugin plugin;
    @NonNull
    private final int pkgId;
    @NonNull
    private final Player player;
    private static final Gson gson = new Gson();

    @Override
    public void run() {
        CheckoutUrlResponse response;
        try {
            response = plugin.getApiClient().getCheckoutUri(player.getName(), pkgId);
        } catch (IOException | ApiException e) {
            player.sendMessage(ChatColor.RED + plugin.getI18n().get("cant_check_out"));
            return;
        }

        player.sendMessage(ChatColor.STRIKETHROUGH + "                                            ");

        // We have to manually generate JSON.
        JsonObject messageObject = new JsonObject();
        messageObject.addProperty("text", plugin.getI18n().get("to_buy_this_package"));
        messageObject.addProperty("color", "green");
        JsonObject clickEventObject = new JsonObject();
        clickEventObject.addProperty("action", "open_url");
        clickEventObject.addProperty("value", response.getUrl());
        messageObject.add("clickEvent", clickEventObject);
        ChatAPI.sendRawMessage(player, gson.toJson(messageObject));

        player.sendMessage(ChatColor.STRIKETHROUGH + "                                            ");
    }
}
