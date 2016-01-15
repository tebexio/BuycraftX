package net.buycraft.plugin.bukkit.tasks;

import lombok.Getter;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.DueQueueInformation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static net.buycraft.plugin.bukkit.tasks.CommandExecutor.mojangUuidToJavaUuid;

public class DuePlayerFetcher implements Runnable {
    private final BuycraftPlugin plugin;
    private final Map<String, QueuedPlayer> due = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    @Getter
    private final AtomicBoolean inProgress = new AtomicBoolean(false);

    public DuePlayerFetcher(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getApiClient() == null) {
            return; // no API client
        }

        if (!inProgress.compareAndSet(false, true)) {
            plugin.getLogger().info("Already fetching due player information!");
            return;
        }

        DueQueueInformation information;
        try {
            information = plugin.getApiClient().retrieveDueQueue();
        } catch (IOException | ApiException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not fetch due players queue", e);
            return;
        }

        plugin.getLogger().info(String.format("Fetched due players (%d in queue).", information.getPlayers().size()));

        // Issue immediate task if required.
        if (information.getMeta().isExecuteOffline()) {
            plugin.getLogger().info("Executing commands that can be completed now...");
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new ImmediateExecutionRunner(plugin));
        }

        lock.lock();
        try {
            due.clear();
            for (QueuedPlayer player : information.getPlayers()) {
                due.put(player.getName().toLowerCase(Locale.US), player);
            }
        } finally {
            lock.unlock();
        }

        inProgress.set(false);

        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                // Check for online players and execute their commands.
                List<QueuedPlayer> processNow = new ArrayList<>();

                lock.lock();
                try {
                    for (Iterator<QueuedPlayer> it = due.values().iterator(); it.hasNext(); ) {
                        QueuedPlayer qp = it.next();
                        Player player;
                        if (qp.getUuid() != null) {
                            player = Bukkit.getPlayer(mojangUuidToJavaUuid(qp.getUuid()));
                        } else {
                            player = Bukkit.getPlayer(qp.getName());
                        }
                        if (player != null) {
                            processNow.add(qp);
                            it.remove();
                        }
                    }
                } finally {
                    lock.unlock();
                }

                if (!processNow.isEmpty()) {
                    plugin.getLogger().info(String.format("Executing commands for %d online players...", processNow.size()));
                    for (int i = 0; i < processNow.size(); i++) {
                        QueuedPlayer qp = processNow.get(i);
                        // 500ms delay between each player to spread server load for many online players
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new PlayerLoginExecution(qp, plugin), 10 * i);
                    }
                }
            }
        });
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, 20 * information.getMeta().getNextCheck());
    }

    public QueuedPlayer fetchAndRemoveDuePlayer(String name) {
        lock.lock();
        try {
            return due.remove(name.toLowerCase(Locale.US));
        } finally {
            lock.unlock();
        }
    }
}
