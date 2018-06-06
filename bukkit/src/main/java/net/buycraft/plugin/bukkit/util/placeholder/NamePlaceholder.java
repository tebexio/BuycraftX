package net.buycraft.plugin.bukkit.util.placeholder;

import net.buycraft.plugin.UuidUtil;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.placeholder.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.regex.Pattern;

public class NamePlaceholder implements Placeholder {
    private static final Pattern REPLACE_NAME = Pattern.compile("[{\\(<\\[](name|player|username)[}\\)>\\]]", Pattern.CASE_INSENSITIVE);

    @Override
    public String replace(String command, QueuedPlayer player, QueuedCommand queuedCommand) {
        if (player.getUuid() == null || player.getUuid() == "") {
            return REPLACE_NAME.matcher(command).replaceAll(player.getName());
        }
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UuidUtil.mojangUuidToJavaUuid(player.getUuid()));

        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return REPLACE_NAME.matcher(command).replaceAll(player.getName());
        }

        return REPLACE_NAME.matcher(command).replaceAll(offlinePlayer.getName());
    }
}
