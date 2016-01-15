package net.buycraft.plugin.bukkit.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.util.CommandExecutorResult;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ExecuteAndConfirmCommandExecutor implements Callable<CommandExecutorResult>, Runnable {
    private final BuycraftPlugin plugin;
    private final QueuedPlayer fallbackPlayer;
    private final List<QueuedCommand> commandList;
    private final boolean requireOnline;
    private final boolean skipDelay;

    @Override
    public CommandExecutorResult call() throws Exception {
        // Perform the actual command execution.
        Future<CommandExecutorResult> initialCheck = Bukkit.getScheduler().callSyncMethod(plugin, new CommandExecutor(
                plugin, fallbackPlayer, commandList, requireOnline, skipDelay));
        CommandExecutorResult result = initialCheck.get();

        plugin.getLogger().info(result.toString());

        if (!result.getDone().isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            for (QueuedCommand command : result.getDone()) {
                ids.add(command.getId());
            }

            plugin.getApiClient().deleteCommand(ids);
        }

        return result;
    }

    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to execute commands", e);
        }
    }
}
