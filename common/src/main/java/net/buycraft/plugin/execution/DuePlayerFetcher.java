package net.buycraft.plugin.execution;

import com.google.common.collect.ImmutableList;
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
    private static final int FALLBACK_CHECK_BACK_SECS = 300;
    private static final int MAXIMUM_ONLINE_PLAYERS_TO_EXECUTE = 60;
    private static final int DELAY_BETWEEN_PLAYERS = 500;
    private final IBuycraftPlatform platform;
    private final Map<String, QueuedPlayer> due = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    private final boolean verbose;
    private final Random random = new Random();

    public boolean inProgress() {
        return inProgress.get();
    }

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
            do {
                try {
                    information = platform.getApiClient().retrieveDueQueue();
                    if(information == null){
                        return;
                    }
                    nextCheck = information.getMeta().getNextCheck();
                } catch (IOException | ApiException e) {
                    platform.log(Level.SEVERE, "Could not fetch due players queue", e);
                    return;
                }

                for (QueuedPlayer player : information.getPlayers()) {
                    // Using Locale.US as servers can sometimes have other locales in use.
                    allDue.put(player.getName().toLowerCase(Locale.US), player);
                }
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

        lock.lock();
        try {
            for (Iterator<QueuedPlayer> it = due.values().iterator(); it.hasNext(); ) {
                QueuedPlayer qp = it.next();
                if (platform.isPlayerOnline(qp)) {
                    if (processNow.size() < MAXIMUM_ONLINE_PLAYERS_TO_EXECUTE) {
                        processNow.add(qp);
                        it.remove();
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        if (!processNow.isEmpty()) {
            if (verbose) {
                platform.log(Level.INFO, String.format("Executing commands for %d online players...", processNow.size()));
            }
            for (int i = 0; i < processNow.size(); i++) {
                QueuedPlayer qp = processNow.get(i);
                platform.executeAsyncLater(new PlayerCommandExecutor(qp, platform), DELAY_BETWEEN_PLAYERS * (i + 1), TimeUnit.MILLISECONDS);
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
