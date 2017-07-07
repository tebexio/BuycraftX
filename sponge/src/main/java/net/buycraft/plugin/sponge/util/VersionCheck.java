package net.buycraft.plugin.sponge.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.data.responses.Version;
import net.buycraft.plugin.shared.util.VersionUtil;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static net.buycraft.plugin.shared.util.VersionUtil.isVersionGreater;

@RequiredArgsConstructor
public class VersionCheck {
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

        lastKnownVersion = VersionUtil.getVersion(plugin.getHttpClient(), "sponge", secret);

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

    @Listener
    public void onPlayerJoinEvent(ClientConnectionEvent.Join event) {
        if (event.getTargetEntity().hasPermission("buycraft.admin") && !upToDate) {
            plugin.getPlatform().executeAsyncLater(() -> {
                try {
                    event.getTargetEntity().sendMessage(
                            Text.builder()
                                    .append(Text.of(plugin.getI18n().get("update_available", lastKnownVersion.getVersion())))
                                    .onClick(TextActions.openUrl(new URL("https://server.buycraft.net")))
                                    .color(TextColors.YELLOW)
                                    .build());
                } catch (MalformedURLException e) {
                    throw new AssertionError(e); // seriously?
                }
            }, 3, TimeUnit.SECONDS);
        }
    }
}
