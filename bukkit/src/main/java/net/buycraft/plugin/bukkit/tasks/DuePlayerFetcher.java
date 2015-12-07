package net.buycraft.plugin.bukkit.tasks;

import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.DueQueueInformation;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class DuePlayerFetcher implements Runnable {
    private final BuycraftPlugin plugin;
    private final Map<String, QueuedPlayer> due = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public DuePlayerFetcher(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getApiClient() == null) {
            return; // no API client
        }

        DueQueueInformation information;
        try {
            information = plugin.getApiClient().retrieveDueQueue();
        } catch (IOException | ApiException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not fetch due players queue", e);
            return;
        }

        // Issue immediate task if required.
        if (information.getMeta().isExecuteOffline()) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new ImmediateExecutionRunner(plugin));
        }

        lock.lock();
        try {
            due.clear();
            for (QueuedPlayer player : information.getPlayers()) {
                due.put(player.getUsername(), player);
            }
        } finally {
            lock.unlock();
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, 20 * information.getMeta().getNextCheck());
    }

    public QueuedPlayer fetchAndRemoveDuePlayer(String name) {
        lock.lock();
        try {
            return due.remove(name);
        } finally {
            lock.unlock();
        }
    }
}
