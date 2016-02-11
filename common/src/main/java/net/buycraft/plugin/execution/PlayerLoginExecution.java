package net.buycraft.plugin.execution;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.QueueInformation;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@RequiredArgsConstructor
public class PlayerLoginExecution implements Runnable {
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

        // Perform the actual command execution.
        CommandExecutorResult result;
        try {
            FutureTask<CommandExecutorResult> f = new FutureTask<>(new CommandExecutor(platform, null, information.getCommands(), true, false));
            platform.executeBlocking(f);
            result = f.get();
        } catch (Exception e) {
            platform.log(Level.SEVERE, "Unable to execute commands", e);
            return;
        }

        if (!result.getQueuedForDelay().isEmpty()) {
            for (Map.Entry<Integer, Collection<QueuedCommand>> entry : result.getQueuedForDelay().asMap().entrySet()) {
                platform.executeAsyncLater(new CommandExecutor(platform, player, ImmutableList.copyOf(entry.getValue()), true, true),
                        entry.getKey(), TimeUnit.SECONDS);
            }
        }
    }
}
