package net.buycraft.plugin.bukkit.tasks;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.util.CommandExecutorResult;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.QueueInformation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

        if (!result.getDone().isEmpty()) {
            try {
                List<Integer> ids = new ArrayList<>();
                for (QueuedCommand command : result.getDone()) {
                    ids.add(command.getId());
                }

                plugin.getApiClient().deleteCommand(ids);
            } catch (IOException | ApiException e) {
                plugin.getLogger().log(Level.SEVERE, "Unable to mark commands as finished", e);
                return;
            }
        }
    }

    private int getFreeInventorySlots(Inventory inventory) {
        int s = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack == null)
                s++;
        }
        return s;
    }

    private UUID mojangUuidToJavaUuid(String id) {
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkArgument(id.matches("^[a-z0-9]{32}"), "Not a valid Mojang UUID.");

        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" +
                id.substring(16, 20) + "-" + id.substring(20, 32));
    }

    @Data
    private class Result {
        private final List<Integer> done;
        private final List<Integer> toQueue;
    }
}
