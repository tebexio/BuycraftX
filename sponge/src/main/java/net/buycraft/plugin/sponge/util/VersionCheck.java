package net.buycraft.plugin.sponge.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.data.responses.Version;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.util.VersionUtil;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class VersionCheck {
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

        lastKnownVersion = VersionUtil.getVersion(plugin.getHttpClient(), "sponge");

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

    @Listener
    public void onPlayerJoinEvent(ClientConnectionEvent.Join event) {
        if (event.getTargetEntity().hasPermission("buycraft.admin") && !upToDate) {
            plugin.getPlatform().executeAsyncLater(() -> {
                try {
                    event.getTargetEntity().sendMessage(
                            Text.builder()
                                .append(Text.of(String.format("A new version of BuycraftX (%s) is available. Go to your server panel at ",
                                        lastKnownVersion.getVersion())))
                                .color(TextColors.YELLOW)
                                .append(Text.of("https://server.buycraft.net"))
                                .onClick(TextActions.openUrl(new URL("https://server.buycraft.net")))
                                .color(TextColors.YELLOW)
                                .append(Text.of(" to update."))
                                .build());
                } catch (MalformedURLException e) {
                    throw new AssertionError(e); // seriously?
                }
            }, 3, TimeUnit.SECONDS);
        }
    }
}
