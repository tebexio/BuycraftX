package net.buycraft.plugin.sponge.util;

import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.Sponge;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeenUtils {
    public static void postServerInformation(BuycraftPlugin plugin) {
        Map<String, Object> serverData = new LinkedHashMap<>();
        Map<String, Object> accountData = new LinkedHashMap<>();
        Map<String, Object> pluginData = new LinkedHashMap<>();

        // Server data
        serverData.put("id", plugin.getServerInformation().getServer().getId());
        serverData.put("platform", "bukkit");
        serverData.put("platform_version", Sponge.getPlatform().getMinecraftVersion());
        serverData.put("online_mode", Sponge.getServer().getOnlineMode());

        // Plugin data
        pluginData.put("version", Sponge.getPluginManager().fromInstance(plugin).get().getVersion());

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
