package net.buycraft.plugin.execution.strategy;

import com.google.common.collect.Lists;
import net.buycraft.plugin.IBuycraftPlatform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class PostCompletedCommandsTask implements Runnable {
    private static final int MAXIMUM_COMMANDS_TO_POST = 50;
    private final Queue<Integer> completed = new ConcurrentLinkedQueue<>();
    private final ArrayList<Integer> retainedCompleted = new ArrayList<>();
    private final IBuycraftPlatform platform;

    public PostCompletedCommandsTask(final IBuycraftPlatform platform) {
        this.platform = platform;
    }

    @Override
    public void run() {
        List<Integer> commandsToPost = new ArrayList<>();
        while (commandsToPost.size() < MAXIMUM_COMMANDS_TO_POST) {
            Integer posted = completed.poll();
            if (posted == null) break;
            commandsToPost.add(posted);
        }
        if (!commandsToPost.isEmpty()) {
            try {
                platform.getApiClient().deleteCommands(commandsToPost).execute();
            } catch (IOException e) {
                platform.log(Level.SEVERE, "Unable to mark commands as completed", e);
                // TODO: Retry?
            }
        }
    }

    public void add(Integer id) {
        completed.add(id);
        retainedCompleted.add(id);
    }

    public ArrayList getRetained() {
        return this.retainedCompleted;
    }

    public void flush() {
        if (!completed.isEmpty()) {
            for (List<Integer> list : Lists.partition(new ArrayList<>(completed), MAXIMUM_COMMANDS_TO_POST)) {
                try {
                    platform.getApiClient().deleteCommands(list).execute();
                } catch (IOException e) {
                    platform.log(Level.SEVERE, "Unable to mark commands as completed", e);
                    break;
                }
            }
        }
    }
}
