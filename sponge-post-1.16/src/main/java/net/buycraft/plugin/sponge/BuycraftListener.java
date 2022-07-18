package net.buycraft.plugin.sponge;

import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.ServerEvent;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.util.Date;

public class BuycraftListener {
    private final BuycraftPlugin plugin;

    public BuycraftListener(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onPlayerJoinEvent(ServerSideConnectionEvent.Join event) {
        if (plugin.getApiClient() == null) {
            return;
        }

        ServerPlayer player = event.player();
        plugin.getServerEventSenderTask().queueEvent(new ServerEvent(
                player.uniqueId().toString().replace("-", ""),
                player.name(),
                player.connection().address().getAddress().getHostAddress(),
                ServerEvent.JOIN_EVENT,
                new Date()
        ));

        QueuedPlayer qp = plugin.getDuePlayerFetcher().fetchAndRemoveDuePlayer(player.name());
        if (qp != null) {
            plugin.getPlayerJoinCheckTask().queue(qp);
        }
    }

    @Listener
    public void onPlayerQuitEvent(ServerSideConnectionEvent.Disconnect event) {
        if (plugin.getApiClient() == null) {
            return;
        }

        ServerPlayer player = event.player();
        plugin.getServerEventSenderTask().queueEvent(new ServerEvent(
                player.uniqueId().toString().replace("-", ""),
                player.name(),
                player.connection().address().getAddress().getHostAddress(),
                ServerEvent.LEAVE_EVENT,
                new Date()
        ));
    }
}
