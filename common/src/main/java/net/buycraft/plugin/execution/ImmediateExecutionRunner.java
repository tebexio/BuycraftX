package net.buycraft.plugin.execution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.responses.QueueInformation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ImmediateExecutionRunner implements Runnable {
    private final IBuycraftPlatform platform;
    private final Set<Integer> executingLater = Sets.newConcurrentHashSet();
    private final Random random = new Random();

    @Override
    public void run() {
        if (platform.getApiClient() == null) {
            return; // no API client
        }

        QueueInformation information;

        do {
            try {
                information = platform.getApiClient().retrieveOfflineQueue();
            } catch (IOException | ApiException e) {
                platform.log(Level.SEVERE, "Could not fetch command queue", e);
                return;
            }

            // Filter out commands we're going to execute at a later time.
            for (Iterator<QueuedCommand> it = information.getCommands().iterator(); it.hasNext(); ) {
                QueuedCommand command = it.next();
                if (executingLater.contains(command.getId()))
                    it.remove();
            }

            // Nothing to do? Then let's exit.
            if (information.getCommands().isEmpty()) {
                return;
            }

            // Perform the actual command execution.
            CommandExecutorResult result;
            try {
                result = new CommandExecutor(platform, null, information.getCommands(), false, false).call();
            } catch (Exception e) {
                platform.log(Level.SEVERE, "Unable to execute commands", e);
                return;
            }

            if (!result.getQueuedForDelay().isEmpty()) {
                for (QueuedCommand command : result.getQueuedForDelay().values()) {
                    executingLater.add(command.getId());
                }

                for (Map.Entry<Integer, Collection<QueuedCommand>> entry : result.getQueuedForDelay().asMap().entrySet()) {
                    final List<QueuedCommand> toRun = ImmutableList.copyOf(entry.getValue());
                    platform.executeAsyncLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new CommandExecutor(platform, null, toRun, false, true).run();
                            } finally {
                                for (QueuedCommand command : toRun) {
                                    executingLater.remove(command.getId());
                                }
                            }
                        }
                    }, entry.getKey(), TimeUnit.SECONDS);
                }
            }

            // Sleep for between 0.5-1.5 seconds to help spread load.
            try {
                Thread.sleep(500 + random.nextInt(1000));
            } catch (InterruptedException e) {
                // Shouldn't happen, but in that case just bail out.
                return;
            }
        } while (information.getMeta().isLimited());
    }
}
