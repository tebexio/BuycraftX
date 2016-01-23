package net.buycraft.plugin.bukkit.util;

import lombok.experimental.UtilityClass;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class KeenUtils {
    public static void postServerInformation(BuycraftPlugin plugin) {
        Map<String, Object> keenData = new HashMap<>();

        // account id, server id, plugin version, server version, online/offline mode, platform
        keenData.put("account_id", plugin.getServerInformation().getAccount().getId());
        keenData.put("server_id", plugin.getServerInformation().getServer().getId());
        keenData.put("plugin_version", plugin.getDescription().getVersion());
        keenData.put("server_platform", "bukkit");
        keenData.put("server_version", Bukkit.getVersion());
        keenData.put("online_mode", Bukkit.getOnlineMode());

        plugin.getKeenClient().addEvent("server_startups", keenData);
    }
}
