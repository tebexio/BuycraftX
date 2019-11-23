package net.buycraft.plugin.execution;

import com.google.common.collect.Lists;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.ServerEvent;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class ServerEventSenderTask implements Runnable {

    private final IBuycraftPlatform platform;
    private final boolean verbose;

    private final List<ServerEvent> eventQueue = Lists.newArrayList();

    private boolean stopped = false;

    public ServerEventSenderTask(IBuycraftPlatform platform, boolean verbose) {
        this.platform = platform;
        this.verbose = verbose;
    }

    @Override
    public void run() {
        if (platform.getApiClient() == null) {
            return;
        }

        if(platform.getServerInformation() == null || !platform.getServerInformation().getAccount().isLogEvents()) {
            return;
        }

        if(eventQueue.size() == 0) {
            return;
        }

        List<ServerEvent> runEvents = Lists.newArrayList(eventQueue.subList(0, Math.min(eventQueue.size(), 750)));

        try {
            if (verbose) platform.log(Level.INFO, "Sending " + runEvents.size() + " analytic events");
            platform.getApiClient().sendEvents(runEvents).execute();
        } catch (IOException e) {
            platform.log(Level.SEVERE, "Failed to send analytic events!", e);
            return;
        }

        synchronized (eventQueue) {
            runEvents.forEach(eventQueue::remove);
        }
    }

    public void queueEvent(ServerEvent event) {
        if(stopped) return;;
        if(platform.getServerInformation() == null || !platform.getServerInformation().getAccount().isLogEvents()) return;
        synchronized (eventQueue) {
            eventQueue.add(event);
        }
    }

    public void stop() {
        stopped = true;
    }
}
