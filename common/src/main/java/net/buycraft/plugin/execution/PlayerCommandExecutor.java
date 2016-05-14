package net.buycraft.plugin.execution;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.QueueInformation;
import net.buycraft.plugin.execution.strategy.ToRunQueuedCommand;

import java.io.IOException;
import java.util.logging.Level;

@RequiredArgsConstructor
public class PlayerCommandExecutor implements Runnable {
    private final QueuedPlayer player;
    private final IBuycraftPlatform platform;

    @Override
    public void run() {
        QueueInformation information;
        try {
            information = platform.getApiClient().getPlayerQueue(player.getId());
        } catch (IOException | ApiException e) {
            // TODO: Implement retry logic.
            platform.log(Level.SEVERE, "Could not fetch command queue for player", e);
            return;
        }

        platform.log(Level.INFO, String.format("Fetched %d commands for player '%s'.", information.getCommands().size(), player.getName()));

        // Queue commands for later.
        for (QueuedCommand command : information.getCommands()) {
            platform.getExecutor().queue(new ToRunQueuedCommand(player, command, true));
        }
    }
}
