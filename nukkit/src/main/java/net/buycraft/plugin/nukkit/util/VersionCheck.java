package net.buycraft.plugin.nukkit.util;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.data.responses.Version;
import net.buycraft.plugin.nukkit.BuycraftPlugin;
import net.buycraft.plugin.shared.util.VersionUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static net.buycraft.plugin.shared.util.VersionUtil.isVersionGreater;

@RequiredArgsConstructor
public class VersionCheck implements Listener {
    private final BuycraftPlugin plugin;
    private final String pluginVersion;
    private final String secret;
    @Getter
    private Version lastKnownVersion;
    @Getter
    private boolean upToDate = true;

    public void verify() throws IOException {
        if (pluginVersion.endsWith("-SNAPSHOT")) {
            return; // SNAPSHOT versions ignore updates
        }

        lastKnownVersion = VersionUtil.getVersion(plugin.getHttpClient(), "nukkit", secret);

        if (lastKnownVersion == null) {
            return;
        }

        // Compare versions
        String latestVersionString = lastKnownVersion.getVersion();

        if (!latestVersionString.equals(pluginVersion)) {
            upToDate = !isVersionGreater(pluginVersion, latestVersionString);

            if (!upToDate) {
                plugin.getLogger().info(plugin.getI18n().get("update_available", lastKnownVersion.getVersion()));
            }
        }
    }

    @EventHandler
    public void onPostLogin(final PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("buycraft.admin") && !upToDate) {
            plugin.getPlatform().executeAsyncLater(new Runnable() {
                @Override
                public void run() {
                    event.getPlayer().sendMessage(TextFormat.YELLOW + plugin.getI18n().get("update_available", lastKnownVersion.getVersion()));
                }
            }, 3, TimeUnit.SECONDS);
        }
    }
}
