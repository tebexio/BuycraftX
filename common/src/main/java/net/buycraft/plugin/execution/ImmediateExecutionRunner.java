package net.buycraft.plugin.execution;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.responses.QueueInformation;
import net.buycraft.plugin.execution.strategy.ToRunQueuedCommand;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ImmediateExecutionRunner implements Runnable {
    private final IBuycraftPlatform platform;
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

            // Nothing to do? Then let's exit.
            if (information.getCommands().isEmpty()) {
                return;
            }

            // Queue commands for later.
            for (QueuedCommand command : information.getCommands()) {
                platform.getExecutor().queue(new ToRunQueuedCommand(command.getPlayer(), command, false));
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
