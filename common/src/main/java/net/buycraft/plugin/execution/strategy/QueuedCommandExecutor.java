package net.buycraft.plugin.execution.strategy;

import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.platform.NoBlocking;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class QueuedCommandExecutor implements CommandExecutor, Runnable {
    private static final long MAXIMUM_EXECUTION_TIME = TimeUnit.MILLISECONDS.toNanos(5);

    private final IBuycraftPlatform platform;
    private final boolean blocking;
    private final Queue<ToRunQueuedCommand> commandQueue = new ConcurrentLinkedQueue<>();

    public QueuedCommandExecutor(IBuycraftPlatform platform) {
        this.platform = platform;
        this.blocking = !platform.getClass().isAnnotationPresent(NoBlocking.class);
    }

    @Override
    public void queue(ToRunQueuedCommand command) {
        commandQueue.add(command);
    }

    @Override
    public void run() {
        final List<Integer> couldRun = new ArrayList<>();

        long start = System.nanoTime();
        int run = 0;
        Iterator<ToRunQueuedCommand> it = commandQueue.iterator();
        while (System.nanoTime() - start <= MAXIMUM_EXECUTION_TIME && it.hasNext()) {
            if (blocking && run > 15) {
                break; // We have run too many commands, run more later
            }

            ToRunQueuedCommand command = it.next();
            if (command.canExecute(platform)) {
                // Run the command now.
                String finalCommand = platform.getPlaceholderManager().doReplace(command.getPlayer(), command.getCommand());
                platform.log(Level.INFO, String.format("Dispatching command '%s' for player '%s'.", finalCommand, command.getPlayer().getName()));
                try {
                    platform.dispatchCommand(finalCommand);
                    couldRun.add(command.getCommand().getId());
                } catch (Exception e) {
                    platform.log(Level.SEVERE, String.format("Could not dispatch command '%s' for player '%s'. " +
                            "This is typically a plugin error, not an issue with BuycraftX.", finalCommand, command.getPlayer().getName()), e);
                }

                it.remove();
            }

            run++;
        }

        long fullTime = System.nanoTime() - start;
        // +1ms to account for timing
        if (fullTime > MAXIMUM_EXECUTION_TIME + TimeUnit.MILLISECONDS.toNanos(1)) {
            // Make the time much nicer.
            BigDecimal timeMs = new BigDecimal(fullTime).divide(new BigDecimal("1000000"), 2, BigDecimal.ROUND_CEILING);
            if (blocking) {
                platform.log(Level.SEVERE, "Command execution took " + timeMs.toPlainString() + "ms to complete. " +
                        "This indicates an issue with one of your server's plugins, which can cause lag.");
            } else {
                platform.log(Level.SEVERE, "Command execution took " + timeMs.toPlainString() + "ms to complete. " +
                        "This indicates an issue with one of your server's plugins, which will slow command execution.");
            }
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
