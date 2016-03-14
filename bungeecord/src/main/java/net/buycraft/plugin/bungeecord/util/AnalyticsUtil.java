package net.buycraft.plugin.bungeecord.util;

import lombok.experimental.UtilityClass;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.buycraft.plugin.util.AnalyticsSend;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

@UtilityClass
public class AnalyticsUtil {
    public static void postServerInformation(BuycraftPlugin plugin) {
        Map<String, Object> serverData = new LinkedHashMap<>();
        Map<String, Object> pluginData = new LinkedHashMap<>();

        // Server data
        serverData.put("platform", "bungeecord");
        serverData.put("platform_version", plugin.getProxy().getVersion());
        serverData.put("online_mode", plugin.getProxy().getConfig().isOnlineMode());

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
