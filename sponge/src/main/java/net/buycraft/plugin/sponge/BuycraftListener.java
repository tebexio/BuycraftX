package net.buycraft.plugin.sponge;

import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.ServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Date;

public class BuycraftListener {
    private final BuycraftPlugin plugin;

    public BuycraftListener(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onPlayerJoinEvent(ClientConnectionEvent.Join event) {
        if (plugin.getApiClient() == null) {
            return;
        }

        plugin.getServerEventSenderTask().queueEvent(new ServerEvent(
                event.getTargetEntity().getUniqueId().toString().replace("-", ""),
                event.getTargetEntity().getName(),
                event.getTargetEntity().getConnection().getAddress().getHostName(),
                ServerEvent.JOIN_EVENT,
                new Date()
        ));

        QueuedPlayer qp = plugin.getDuePlayerFetcher().fetchAndRemoveDuePlayer(event.getTargetEntity().getName());
        if (qp != null) {
            plugin.getPlayerJoinCheckTask().queue(qp);
        }
    }

    @Listener
    public void onPlayerQuitEvent(ClientConnectionEvent.Disconnect event) {
        if (plugin.getApiClient() == null) {
            return;
        }

        plugin.getServerEventSenderTask().queueEvent(new ServerEvent(
                event.getTargetEntity().getUniqueId().toString().replace("-", ""),
                event.getTargetEntity().getName(),
                event.getTargetEntity().getConnection().getAddress().getHostName(),
                ServerEvent.LEAVE_EVENT,
                new Date()
        ));
    }
}
