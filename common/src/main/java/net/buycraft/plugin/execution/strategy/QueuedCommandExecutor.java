package net.buycraft.plugin.execution.strategy;

import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.platform.NoBlocking;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class QueuedCommandExecutor implements CommandExecutor, Runnable {
    private static final long MAXIMUM_NOTIFICATION_TIME = TimeUnit.MILLISECONDS.toNanos(5);
    private static final int RUN_MAX_COMMANDS_BLOCKING = 10;

    private final IBuycraftPlatform platform;
    private final boolean blocking;
    private final Queue<ToRunQueuedCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private final PostCompletedCommandsTask completedCommandsTask;

    public QueuedCommandExecutor(IBuycraftPlatform platform, PostCompletedCommandsTask completedCommandsTask) {
        this.platform = platform;
        this.blocking = !platform.getClass().isAnnotationPresent(NoBlocking.class);
        this.completedCommandsTask = completedCommandsTask;
    }

    @Override
    public void queue(ToRunQueuedCommand command) {
        if (!commandQueue.contains(command))
            commandQueue.add(command);
    }

    @Override
    public void run() {
        long start = System.nanoTime();
        int run = 0;

        for (Iterator<ToRunQueuedCommand> it = commandQueue.iterator(); it.hasNext(); ) {
            if (blocking && run >= RUN_MAX_COMMANDS_BLOCKING) {
                break; // We have run too many commands, run more later
            }

            ToRunQueuedCommand command = it.next();
            if (command.canExecute(platform)) {
                // Run the command now.
                String finalCommand = platform.getPlaceholderManager().doReplace(command.getPlayer(), command.getCommand());
                platform.log(Level.INFO, String.format("Dispatching command '%s' for player '%s'.", finalCommand, command.getPlayer().getName()));
                try {
                    platform.dispatchCommand(finalCommand);
                    completedCommandsTask.add(command.getCommand().getId());
                } catch (Exception e) {
                    platform.log(Level.SEVERE, String.format("Could not dispatch command '%s' for player '%s'. " +
                            "This is typically a plugin error, not an issue with BuycraftX.", finalCommand, command.getPlayer().getName()), e);
                }

                it.remove();
                run++;
            }
        }

        long fullTime = System.nanoTime() - start;
        if (fullTime > MAXIMUM_NOTIFICATION_TIME) {
            // Make the time much nicer.
            BigDecimal timeMs = new BigDecimal(fullTime).divide(new BigDecimal("1000000"), 2, BigDecimal.ROUND_CEILING);
            if (blocking) {
                platform.log(Level.SEVERE, "Command execution took " + timeMs.toPlainString() + "ms to complete. " +
                        "This likely indicates an issue with one of your server's plugins, which can cause lag.");
            } else {
                platform.log(Level.SEVERE, "Command execution took " + timeMs.toPlainString() + "ms to complete. " +
                        "This likely indicates an issue with one of your server's plugins, which will slow command execution.");
            }
        }
    }
}
