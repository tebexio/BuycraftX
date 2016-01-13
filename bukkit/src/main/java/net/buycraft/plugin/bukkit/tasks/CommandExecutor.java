package net.buycraft.plugin.bukkit.tasks;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.util.CommandExecutorResult;
import net.buycraft.plugin.data.QueuedCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class CommandExecutor implements Callable<CommandExecutorResult> {
    private final BuycraftPlugin plugin;
    private final List<QueuedCommand> commands;
    private final boolean requireOnline;
    private final boolean skipDelay;

    @Override
    public CommandExecutorResult call() throws Exception {
        List<QueuedCommand> canRun = new ArrayList<>();
        List<QueuedCommand> queuedForOnline = new ArrayList<>();
        ListMultimap<Integer, QueuedCommand> delayed = ArrayListMultimap.create();

        // Determine what we can run.
        for (QueuedCommand command : commands) {
            Player player = Bukkit.getPlayer(mojangUuidToJavaUuid(command.getPlayer().getUuid()));
            if (player == null && requireOnline) {
                queuedForOnline.add(command);
                continue;
            }

            Integer requiredSlots = command.getConditions().get("slots");
            if (requiredSlots != null && player != null) {
                int free = getFreeInventorySlots(player.getInventory());
                if (free < requiredSlots) {
                    queuedForOnline.add(command);
                    continue;
                }
            }

            Integer delay = command.getConditions().get("delay");
            if (delay != null && !skipDelay) {
                delayed.put(delay, command);
            }
        }

        // Now we can run the commands.
        for (QueuedCommand command : canRun) {
            String finalCommand = plugin.getPlaceholderManager().doReplace(command);
            plugin.getLogger().info(String.format("Dispatching command %s for player '%s'.", finalCommand, command.getPlayer().getUsername()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        }

        return new CommandExecutorResult(canRun, queuedForOnline, delayed);
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
}
