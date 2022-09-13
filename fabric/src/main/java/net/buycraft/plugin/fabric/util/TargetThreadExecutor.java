package net.buycraft.plugin.fabric.util;

import net.buycraft.plugin.fabric.BuycraftPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class TargetThreadExecutor implements Executor {
    private final Thread targetThread = Thread.currentThread();
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void execute(@NotNull Runnable command) {
        if (Thread.currentThread() == targetThread) {
            command.run();
        } else {
            queue.offer(command);
        }
    }

    public void run() {
        while (true) {
            Runnable task = queue.poll();
            if (task == null) return;
            try {
                task.run();
            } catch (Exception e) {
                BuycraftPlugin.LOGGER.error("Error executing task", e);
            }
        }
    }
}
