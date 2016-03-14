package net.buycraft.plugin.sponge.util;

import lombok.experimental.UtilityClass;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.util.AnalyticsSend;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public class AnalyticsUtil {
    public static void postServerInformation(BuycraftPlugin plugin) {
        Map<String, Object> serverData = new LinkedHashMap<>();
        Map<String, Object> pluginData = new LinkedHashMap<>();

        // Server data
        serverData.put("platform", "sponge");
        serverData.put("platform_version", Sponge.getPlatform().getMinecraftVersion().getName());
        serverData.put("online_mode", Sponge.getServer().getOnlineMode());

        // Plugin data
        pluginData.put("version", plugin.getClass().getAnnotation(Plugin.class).version());

        // Combine and send to Buycraft
        Map<String, Object> keenData = new LinkedHashMap<>();
        keenData.put("server", serverData);
        keenData.put("plugin", pluginData);

        try {
            AnalyticsSend.sendAnalytics(plugin.getHttpClient(), plugin.getConfiguration().getServerKey(), keenData);
        } catch (IOException e) {
            plugin.getLogger().warn("Can't send analytics", e);
        }
    }
}
