package net.buycraft.plugin.shared.tasks;

import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.PlayerCommandExecutor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerJoinCheckTask implements Runnable {
    private final IBuycraftPlatform platform;
    private final Queue<QueuedPlayer> queuedPlayers = new ConcurrentLinkedQueue<>();

    public PlayerJoinCheckTask(IBuycraftPlatform platform) {
        this.platform = platform;
    }

    @Override
    public void run() {
        QueuedPlayer qp = queuedPlayers.poll();
        if (qp != null) {
            platform.executeAsync(new PlayerCommandExecutor(qp, platform));
        }
    }

    public void queue(QueuedPlayer player) {
        queuedPlayers.add(player);
    }
}
