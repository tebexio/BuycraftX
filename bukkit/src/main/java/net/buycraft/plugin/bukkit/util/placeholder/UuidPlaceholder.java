package net.buycraft.plugin.bukkit.util.placeholder;

import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;

import java.util.regex.Pattern;

public class UuidPlaceholder implements Placeholder {
    private static final Pattern REPLACE_UUID = Pattern.compile("[{\\(<\\[]uuid[}\\)>\\]]", Pattern.CASE_INSENSITIVE);

    @Override
    public String replace(String command, QueuedPlayer player, QueuedCommand queuedCommand) {
        if (player.getUuid() == null) {
            return command; // can't replace UUID for offline mode
        }
        return REPLACE_UUID.matcher(command).replaceAll(player.getUuid());
    }
}
