package net.buycraft.plugin.execution.strategy;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

@RequiredArgsConstructor
public class PostCompletedCommandsTask implements Runnable {
    private static final int MAXIMUM_COMMANDS_TO_POST = 100;
    private final Queue<Integer> completed = new ConcurrentLinkedQueue<>();
    private final IBuycraftPlatform platform;

    @Override
    public void run() {
        List<Integer> commandsToPost = new ArrayList<>();
        while (commandsToPost.size() < MAXIMUM_COMMANDS_TO_POST) {
            Integer posted = completed.poll();
            if (posted == null)
                break;
            commandsToPost.add(posted);
        }

        if (!commandsToPost.isEmpty()) {
            try {
                platform.getApiClient().deleteCommand(commandsToPost);
            } catch (IOException | ApiException e) {
                platform.log(Level.SEVERE, "Unable to mark commands as completed", e);
                // TODO: Retry?
            }
        }
    }

    public void add(Integer id) {
        completed.add(id);
    }

    public void flush() {
        if (!completed.isEmpty()) {
            for (List<Integer> list : Lists.partition(new ArrayList<>(completed), MAXIMUM_COMMANDS_TO_POST)) {
                try {
                    platform.getApiClient().deleteCommand(list);
                } catch (IOException | ApiException e) {
                    platform.log(Level.SEVERE, "Unable to mark commands as completed", e);
                    break;
                }
            }
        }
    }
}
