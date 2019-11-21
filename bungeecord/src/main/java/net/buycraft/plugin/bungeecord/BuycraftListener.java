package net.buycraft.plugin.bungeecord;

import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.ServerEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Date;

public class BuycraftListener implements Listener {
    private final BuycraftPlugin plugin;

    public BuycraftListener(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        if (plugin.getApiClient() == null) {
            return;
        }

        plugin.getServerEventSenderTask().queueEvent(new ServerEvent(
                event.getPlayer().getUniqueId().toString().replace("-", ""),
                event.getPlayer().getName(),
                ServerEvent.JOIN_EVENT,
                new Date()
        ));

        QueuedPlayer qp = plugin.getDuePlayerFetcher().fetchAndRemoveDuePlayer(event.getPlayer().getName());
        if (qp != null) {
            plugin.getPlayerJoinCheckTask().queue(qp);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        if (plugin.getApiClient() == null) {
            return;
        }

        plugin.getServerEventSenderTask().queueEvent(new ServerEvent(
                event.getPlayer().getUniqueId().toString().replace("-", ""),
                event.getPlayer().getName(),
                ServerEvent.LEAVE_EVENT,
                new Date()
        ));
    }
}
