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
import org.inventivetalent.chat.Reflection;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

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

    static NMSClassResolver nmsClassResolver = new NMSClassResolver();

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
        if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1)) {
            ChatAPI.sendRawMessage(player, gson.toJson(messageObject));
        } else {
            trySendFor17(player, gson.toJson(messageObject));
        }

        player.sendMessage(ChatColor.STRIKETHROUGH + "                                            ");
    }

    private static void trySendFor17(Player player, String json) {
        Class<?> nmsIChatBaseComponent = Reflection.getNMSClass("IChatBaseComponent");
        Class<?> nmsChatSerializer;
        try {
            nmsChatSerializer = nmsClassResolver.resolve(new String[]{"ChatSerializer", "IChatBaseComponent$ChatSerializer"});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Class<?> nmsPacketPlayOutChat = Reflection.getNMSClass("PacketPlayOutChat");
        try {
            Object e = Reflection.getHandle(player);
            Object connection = Reflection.getField(e.getClass(), "playerConnection").get(e);
            Object serialized = Reflection.getMethod(nmsChatSerializer, "a", new Class[]{String.class}).invoke((Object)null, new Object[]{json});
            Object packet = nmsPacketPlayOutChat.getConstructor(new Class[]{nmsIChatBaseComponent}).newInstance(new Object[]{serialized});

            if(packet != null) {
                Reflection.getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[]{packet});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
