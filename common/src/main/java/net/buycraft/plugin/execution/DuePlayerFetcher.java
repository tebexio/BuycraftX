package net.buycraft.plugin.execution;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.DueQueueInformation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

@RequiredArgsConstructor
public class DuePlayerFetcher implements Runnable {
    private final IBuycraftPlatform platform;
    private final Map<String, QueuedPlayer> due = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    @Getter
    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    private final boolean verbose;
    private final Random random = new Random();

    private static final int MAXIMUM_PER_PAGE = 250;
    private static final int FALLBACK_CHECK_BACK_SECS = 300;

    @Override
    public void run() {
        run(true);
    }

    public void run(boolean scheduleAgain) {
        if (platform.getApiClient() == null) {
            return; // no API client
        }

        if (!inProgress.compareAndSet(false, true)) {
            platform.log(Level.INFO, "Already fetching due player information!");
            return;
        }

        int nextCheck = FALLBACK_CHECK_BACK_SECS;

        try {
            if (verbose) {
                platform.log(Level.INFO, "Fetching all due players...");
            }

            Map<String, QueuedPlayer> allDue = new HashMap<>();

            DueQueueInformation information;
            int page = 1;
            do {
                try {
                    information = platform.getApiClient().retrieveDueQueue(MAXIMUM_PER_PAGE, page);
                    nextCheck = information.getMeta().getNextCheck();
                } catch (IOException | ApiException e) {
                    platform.log(Level.SEVERE, "Could not fetch due players queue", e);
                    return;
                }

                for (QueuedPlayer player : information.getPlayers()) {
                    // Using Locale.US as servers can sometimes have other locales in use.
                    allDue.put(player.getName().toLowerCase(Locale.US), player);
                }

                try {
                    Thread.sleep(random.nextInt(1000) + 500);
                } catch (InterruptedException e) {
                    platform.log(Level.SEVERE, "Interrupted", e);
                }

                page++;
            } while (information.getMeta().isMore());

            if (verbose) {
                platform.log(Level.INFO, String.format("Fetched due players (%d found).", allDue.size()));
            }

            // Issue immediate task if required.
            if (information.getMeta().isExecuteOffline()) {
                if (verbose) {
                    platform.log(Level.INFO, "Executing commands that can be completed now...");
                }
                platform.executeAsync(new ImmediateCommandExecutor(platform));
            }

            lock.lock();
            try {
                due.clear();
                due.putAll(allDue);
            } finally {
                lock.unlock();
            }

            processOnlinePlayers();
        } finally {
            inProgress.set(false);
            if (scheduleAgain)
                platform.executeAsyncLater(this, nextCheck, TimeUnit.SECONDS);
        }
    }

    private void processOnlinePlayers() {
        // Check for online players and execute their commands.
        List<QueuedPlayer> processNow = new ArrayList<>();

        for (Iterator<QueuedPlayer> it = due.values().iterator(); it.hasNext(); ) {
            QueuedPlayer qp = it.next();
            if (platform.isPlayerOnline(qp)) {
                processNow.add(qp);
                it.remove();
            }
        }

        if (!processNow.isEmpty()) {
            platform.log(Level.INFO, String.format("Executing commands for %d online players...", processNow.size()));
            for (int i = 0; i < processNow.size(); i++) {
                QueuedPlayer qp = processNow.get(i);
                // 500ms delay between each player to spread server load for many online players, up to a
                // maximum of 5 seconds
                platform.executeAsyncLater(new PlayerCommandExecutor(qp, platform), Math.min(5000, i * 500), TimeUnit.MILLISECONDS);
            }
        }
    }

    public Collection<QueuedPlayer> getDuePlayers() {
        lock.lock();
        try {
            return ImmutableList.copyOf(due.values());
        } finally {
            lock.unlock();
        }
    }

    public QueuedPlayer fetchAndRemoveDuePlayer(String name) {
        lock.lock();
        try {
            // Using Locale.US as servers can sometimes have other locales in use.
            return due.remove(name.toLowerCase(Locale.US));
        } finally {
            lock.unlock();
        }
    }
}
