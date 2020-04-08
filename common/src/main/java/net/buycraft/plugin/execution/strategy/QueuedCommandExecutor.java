package net.buycraft.plugin.execution.strategy;

import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.platform.NoBlocking;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class QueuedCommandExecutor implements CommandExecutor, Runnable {
    private static final long MAXIMUM_NOTIFICATION_TIME = TimeUnit.MILLISECONDS.toNanos(5);
    private final IBuycraftPlatform platform;
    private final boolean blocking;
    private final Set<ToRunQueuedCommand> commandQueue = new LinkedHashSet<>();
    private final PostCompletedCommandsTask completedCommandsTask;
    private int runMaxCommandsBlocking = 10;

    public QueuedCommandExecutor(IBuycraftPlatform platform, PostCompletedCommandsTask completedCommandsTask) {
        this.platform = platform;
        this.blocking = !platform.getClass().isAnnotationPresent(NoBlocking.class);
        this.completedCommandsTask = completedCommandsTask;
    }

    @Override
    public void queue(ToRunQueuedCommand command) {
        synchronized (commandQueue) {
            commandQueue.add(command);
        }
    }

    @Override
    public void run() {
        List<ToRunQueuedCommand> runThisTick = new ArrayList<>();
        synchronized (commandQueue) {
            ArrayList<Integer> queuedCommandIds = new ArrayList<>();
            Set<ToRunQueuedCommand> removeSet = new HashSet<ToRunQueuedCommand>();

            for (ToRunQueuedCommand command : commandQueue) {
                if (queuedCommandIds.contains(command.getCommand().getId())) {
                    removeSet.add(command);
                    continue;
                }
                queuedCommandIds.add(command.getCommand().getId());

                if (command.canExecute(platform)) {
                    runThisTick.add(command);
                    //it.remove();
                    removeSet.add(command);
                }

                if (blocking && runThisTick.size() >= runMaxCommandsBlocking) {
                    break;
                }
            }

            commandQueue.removeAll(removeSet);
        }

        long start = System.nanoTime();
        for (ToRunQueuedCommand command : runThisTick) {
            if (completedCommandsTask.getRetained().contains(command.getCommand().getId())) {
                synchronized (commandQueue) {
                    commandQueue.remove(command);
                }
                continue;
            }

            if (command.canExecute(platform)) {
                String finalCommand = platform.getPlaceholderManager().doReplace(command.getPlayer(), command.getCommand());
                platform.log(Level.INFO, String.format("Dispatching command '%s' for player '%s'.", finalCommand, command.getPlayer().getName()));
                try {
                    platform.dispatchCommand(finalCommand);
                    completedCommandsTask.add(command.getCommand().getId());
                } catch (Exception e) {
                    platform.log(Level.SEVERE, String.format("Could not dispatch command '%s' for player '%s'. " +
                            "This is typically a plugin error, not an issue with BuycraftX.", finalCommand, command.getPlayer().getName()), e);
                }
            }
        }

        long fullTime = System.nanoTime() - start;
        if (fullTime > MAXIMUM_NOTIFICATION_TIME) {
            // Make the time much nicer.
            BigDecimal timeMs = new BigDecimal(fullTime).divide(new BigDecimal("1000000"), 2, BigDecimal.ROUND_CEILING);
            if (blocking) {
                platform.log(Level.INFO, "Command execution took " + timeMs.toPlainString() + "ms to complete. " +
                        "This may indicate an issue with one of your server's plugins, which can cause lag.");
            } else {
                platform.log(Level.INFO, "Command execution took " + timeMs.toPlainString() + "ms to complete. " +
                        "This may indicate an issue with one of your server's plugins, which will slow command execution.");
            }
        }
    }

    public void setRunMaxCommandsBlocking(final int runMaxCommandsBlocking) {
        this.runMaxCommandsBlocking = runMaxCommandsBlocking;
    }
}
