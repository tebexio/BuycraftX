package net.buycraft.plugin.execution;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ExecuteAndConfirmCommandExecutor implements Callable<CommandExecutorResult>, Runnable {
    private final IBuycraftPlatform platform;
    private final QueuedPlayer fallbackPlayer;
    private final List<QueuedCommand> commandList;
    private final boolean requireOnline;
    private final boolean skipDelay;

    @Override
    public CommandExecutorResult call() throws Exception {
        // Perform the actual command execution.

        FutureTask<CommandExecutorResult> initialCheck = new FutureTask<>(new CommandExecutor(
                platform, fallbackPlayer, commandList, requireOnline, skipDelay));
        platform.executeBlocking(initialCheck);
        CommandExecutorResult result = initialCheck.get();

        if (!result.getDone().isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            for (QueuedCommand command : result.getDone()) {
                ids.add(command.getId());
            }

            platform.getApiClient().deleteCommand(ids);
        }

        return result;
    }

    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            platform.log(Level.SEVERE, "Unable to execute commands", e);
        }
    }
}
