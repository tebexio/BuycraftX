package net.buycraft.plugin.bukkit.tasks;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.responses.QueueInformation;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ImmediateExecutionRunner implements Runnable {
    private final BuycraftPlugin plugin;

    @Override
    public void run() {
        if (plugin.getApiClient() == null) {
            return; // no API client
        }

        List<QueuedCommand> commandList = new ArrayList<>();
        QueueInformation information;

        do {
            try {
                information = plugin.getApiClient().retrieveOfflineQueue();
                commandList.addAll(information.getCommands());
            } catch (IOException | ApiException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not fetch command queue for immediate execution", e);
                return;
            }
        } while (!information.getMeta().isLimited());

        // Start running commands.
        // Limit to 20 per tick to help minimize lag.
        for (final List<QueuedCommand> commands : Lists.partition(commandList, 20)) {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    for (QueuedCommand command : commands) {
                        plugin.getLogger().info(String.format("Running command %s for player %s.", command.getCommand(),
                                command.getPlayer().getUsername()));
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.getCommand().replaceAll("\\{name\\}",
                                command.getPlayer().getUsername()));
                    }
                }
            });
        }

        List<Integer> idsToCommit = new ArrayList<>();
        for (QueuedCommand command : commandList) {
            idsToCommit.add(command.getId());
        }

        try {
            // TODO: Add retry.
            plugin.getApiClient().deleteCommand(idsToCommit);
        } catch (IOException | ApiException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not send completed commands", e);
        }
    }
}
