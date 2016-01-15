package net.buycraft.plugin.bukkit.tasks;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.util.CommandExecutorResult;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.QueueInformation;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@RequiredArgsConstructor
public class PlayerLoginExecution implements Runnable {
    private final QueuedPlayer player;
    private final BuycraftPlugin plugin;

    @Override
    public void run() {
        QueueInformation information;
        try {
            information = plugin.getApiClient().getPlayerQueue(player.getId());
        } catch (IOException | ApiException e) {
            // TODO: Implement retry logic.
            plugin.getLogger().log(Level.SEVERE, "Could not fetch command queue for player", e);
            return;
        }

        plugin.getLogger().info(String.format("Fetched %d commands for player '%s'.", information.getCommands().size(), player.getName()));

        // Perform the actual command execution.
        CommandExecutorResult result;
        try {
            result = new ExecuteAndConfirmCommandExecutor(plugin, player, information.getCommands(), true, false).call();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to execute commands", e);
            return;
        }

        if (!result.getQueuedForDelay().isEmpty()) {
            for (Map.Entry<Integer, Collection<QueuedCommand>> entry : result.getQueuedForDelay().asMap().entrySet()) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new ExecuteAndConfirmCommandExecutor(plugin,
                        player, ImmutableList.copyOf(entry.getValue()), true, true), entry.getKey() * 20);
            }
        }
    }
}
