package net.buycraft.plugin.execution.strategy;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@RequiredArgsConstructor
public class QueuedCommandExecutor implements CommandExecutor, Runnable {
    private static final long MAXIMUM_EXECUTION_TIME = TimeUnit.MILLISECONDS.toNanos(5);

    private final IBuycraftPlatform platform;
    private final Queue<ToRunQueuedCommand> commandQueue = new ArrayDeque<>();

    @Override
    public void queue(ToRunQueuedCommand command) {
        commandQueue.add(command);
    }

    @Override
    public void run() {
        final List<Integer> couldRun = new ArrayList<>();

        long start = System.nanoTime();
        Iterator<ToRunQueuedCommand> it = commandQueue.iterator();
        while (System.nanoTime() - start <= MAXIMUM_EXECUTION_TIME && it.hasNext()) {
            ToRunQueuedCommand command = it.next();
            if (command.canExecute(platform)) {
                // Run the command now.
                String finalCommand = platform.getPlaceholderManager().doReplace(command.getPlayer(), command.getCommand());
                platform.log(Level.INFO, String.format("Dispatching command '%s' for player '%s'.", finalCommand, command.getPlayer().getName()));
                try {
                    platform.dispatchCommand(finalCommand);
                    couldRun.add(command.getCommand().getId());
                    it.remove();
                } catch (Exception e) {
                    platform.log(Level.SEVERE, String.format("Could not dispatch command '%s' for player '%s'. " +
                            "This is typically a plugin error, not an issue with BuycraftX.", finalCommand, command.getPlayer().getName()), e);
                }
            }
        }

        long fullTime = System.nanoTime() - start;
        // +1ms to account for timing
        if (fullTime > MAXIMUM_EXECUTION_TIME + TimeUnit.MILLISECONDS.toNanos(1)) {
            // Make the time much nicer.
            BigDecimal timeMs = new BigDecimal(fullTime).divide(new BigDecimal("1000000"), 2, BigDecimal.ROUND_CEILING);
            platform.log(Level.SEVERE, "Command execution took " + timeMs.toPlainString() + "ms to complete. " +
                    "This indicates an issue with one of your server's plugins, which can cause lag.");
        }

        if (!couldRun.isEmpty()) {
            platform.executeAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        platform.getApiClient().deleteCommand(couldRun);
                    } catch (IOException | ApiException e) {
                        platform.log(Level.SEVERE, "Unable to mark commands as completed", e);
                    }
                }
            });
        }
    }
}
