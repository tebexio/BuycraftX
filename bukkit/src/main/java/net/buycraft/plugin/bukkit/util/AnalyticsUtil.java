package net.buycraft.plugin.bukkit.util;

import lombok.experimental.UtilityClass;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.util.AnalyticsSend;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

@UtilityClass
public class AnalyticsUtil {
    private static String getMCVersion(String version) {
        int start = version.indexOf("(MC:");
        return version.substring(start + 5, version.length() - 1);
    }

    public static void postServerInformation(BuycraftPlugin plugin) {
        Map<String, Object> serverData = new LinkedHashMap<>();
        Map<String, Object> pluginData = new LinkedHashMap<>();

        // Server data
        serverData.put("platform", "bukkit");
        serverData.put("platform_version", getMCVersion(Bukkit.getVersion()));
        serverData.put("online_mode", Bukkit.getOnlineMode());

        // Plugin data
        pluginData.put("version", plugin.getDescription().getVersion());

        // Combine and send to Buycraft
        Map<String, Object> keenData = new LinkedHashMap<>();
        keenData.put("server", serverData);
        keenData.put("plugin", pluginData);

        try {
            AnalyticsSend.sendAnalytics(plugin.getHttpClient(), plugin.getConfiguration().getServerKey(), keenData);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Can't send analytics", e);
        }
    }
}
