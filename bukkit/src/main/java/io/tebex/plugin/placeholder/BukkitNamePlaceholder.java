package io.tebex.plugin.placeholder;

import io.tebex.sdk.obj.QueuedPlayer;
import io.tebex.sdk.placeholder.Placeholder;
import io.tebex.sdk.placeholder.PlaceholderManager;
import io.tebex.sdk.util.UUIDUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class BukkitNamePlaceholder implements Placeholder {
    private final PlaceholderManager placeholderManager;

    public BukkitNamePlaceholder(PlaceholderManager placeholderManager) {
        this.placeholderManager = placeholderManager;
    }

    @Override
    public String handle(QueuedPlayer player, String command) {
        if (player.getUuid() == null || player.getUuid().isEmpty()) {
            return placeholderManager.getUsernameRegex().matcher(command).replaceAll(player.getName());
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUIDUtil.mojangIdToJavaId(player.getUuid()));
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return placeholderManager.getUsernameRegex().matcher(command).replaceAll(player.getName());
        }

        return placeholderManager.getUsernameRegex().matcher(command).replaceAll(offlinePlayer.getName());
    }
}