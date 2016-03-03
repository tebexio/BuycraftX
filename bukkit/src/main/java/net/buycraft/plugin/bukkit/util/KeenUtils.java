package net.buycraft.plugin.bukkit.util;

import lombok.experimental.UtilityClass;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public class KeenUtils {
    private static String getMCVersion(String version) {
        int start = version.indexOf("(MC:");
        return version.substring(start + 5, version.length() - 1);
    }

    public static void postServerInformation(BuycraftPlugin plugin) {
        // account id, server id, plugin version, server version, online/offline mode, platform
        Map<String, Object> serverData = new LinkedHashMap<>();
        Map<String, Object> accountData = new LinkedHashMap<>();
        Map<String, Object> pluginData = new LinkedHashMap<>();

        // Server data
        serverData.put("id", plugin.getServerInformation().getServer().getId());
        serverData.put("platform", "bukkit");
        serverData.put("platform_version", getMCVersion(Bukkit.getVersion()));
        serverData.put("online_mode", Bukkit.getOnlineMode());

        // Plugin data
        pluginData.put("version", plugin.getDescription().getVersion());

        // Account data
        accountData.put("id", plugin.getServerInformation().getAccount().getId());

        // Combine and send to Keen IO
        Map<String, Object> keenData = new LinkedHashMap<>();
        keenData.put("server", serverData);
        keenData.put("account", accountData);
        keenData.put("plugin", pluginData);

        plugin.getKeenClient().addEvent("server_startups", keenData);
    }
}
