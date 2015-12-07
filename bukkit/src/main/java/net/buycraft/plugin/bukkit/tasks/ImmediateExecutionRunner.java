package net.buycraft.plugin.bukkit.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.util.CommandExecutorResult;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.responses.QueueInformation;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ImmediateExecutionRunner implements Runnable {
    private final BuycraftPlugin plugin;
    private final Random random = new Random();

    @Override
    public void run() {
        if (plugin.getApiClient() == null) {
            return; // no API client
        }

        QueueInformation information;

        do {
            try {
                information = plugin.getApiClient().retrieveOfflineQueue();
            } catch (IOException | ApiException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not fetch command queue for immediate execution", e);
                return;
            }

            // Perform the actual command execution.
            Future<CommandExecutorResult> initialCheck = Bukkit.getScheduler().callSyncMethod(plugin, new CommandExecutor(
                    plugin, information.getCommands(), false, false));
            CommandExecutorResult result;
            try {
                result = initialCheck.get();
            } catch (InterruptedException | ExecutionException e) {
                plugin.getLogger().log(Level.SEVERE, "Unable to execute commands", e);
                return;
            }

            if (result.getDone().isEmpty()) {
                try {
                    List<Integer> ids = new ArrayList<>();
                    for (QueuedCommand command : result.getDone()) {
                        ids.add(command.getId());
                    }

                    if (!ids.isEmpty()) {
                        plugin.getApiClient().deleteCommand(ids);
                    }
                } catch (IOException | ApiException e) {
                    plugin.getLogger().log(Level.SEVERE, "Unable to mark commands as finished", e);
                    return;
                }
            }

            // Sleep for between 0.5-1.5 seconds to help spread load.
            try {
                Thread.sleep(500 + random.nextInt(1000));
            } catch (InterruptedException e) {
                // Shouldn't happen, but in that case just bail out.
                return;
            }
        } while (!information.getMeta().isLimited());
    }
}
