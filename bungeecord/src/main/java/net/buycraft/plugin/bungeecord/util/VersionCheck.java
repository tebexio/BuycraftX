package net.buycraft.plugin.bungeecord.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.buycraft.plugin.data.responses.Version;
import net.buycraft.plugin.util.VersionUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class VersionCheck implements Listener {
    private final BuycraftPlugin plugin;
    private final String pluginVersion;
    @Getter
    private Version lastKnownVersion;
    @Getter
    private boolean upToDate = true;

    private static final String UPDATE_MESSAGE = "A new version of BuycraftX (%s) is available. Go to your server panel at" +
            " https://server.buycraft.net to update.";

    private boolean isVersionGreater(String one, String two) {
        String[] componentsOne = one.split("\\.");
        String[] componentsTwo = two.split("\\.");

        int verLen = Math.max(componentsOne.length, componentsTwo.length);

        int[] numOne = new int[verLen];
        int[] numTwo = new int[verLen];

        for (int i = 0; i < componentsOne.length; i++) {
            numOne[i] = Integer.parseInt(componentsOne[i]);
        }
        for (int i = 0; i < componentsTwo.length; i++) {
            numTwo[i] = Integer.parseInt(componentsTwo[i]);
        }

        // Quick exclusion check.
        if (Arrays.equals(numOne, numTwo)) {
            return false;
        }

        for (int i = 0; i < numOne.length; i++) {
            if (numTwo[i] == numOne[i])
                continue;

            if (numTwo[i] > numOne[i])
                return true;
        }

        return false;
    }

    public void verify() throws IOException {
        if (pluginVersion.endsWith("-SNAPSHOT")) {
            return; // SNAPSHOT versions ignore updates
        }

        lastKnownVersion = VersionUtil.getVersion(plugin.getHttpClient(), "bungeecord");

        if (lastKnownVersion == null) {
            return;
        }

        // Compare versions
        String latestVersionString = lastKnownVersion.getVersion();

        if (!latestVersionString.equals(pluginVersion)) {
            upToDate = !isVersionGreater(pluginVersion, latestVersionString);

            if (!upToDate) {
                plugin.getLogger().info(String.format(UPDATE_MESSAGE, lastKnownVersion.getVersion()));
            }
        }
    }

    @EventHandler
    public void onPostLogin(final PostLoginEvent event) {
        if (event.getPlayer().hasPermission("buycraft.admin") && !upToDate) {
            plugin.getPlatform().executeAsyncLater(new Runnable() {
                @Override
                public void run() {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + String.format(UPDATE_MESSAGE, lastKnownVersion.getVersion()));
                }
            }, 3, TimeUnit.SECONDS);
        }
    }
}
