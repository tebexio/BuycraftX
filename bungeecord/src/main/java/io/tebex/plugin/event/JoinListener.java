package io.tebex.plugin.event;

import io.tebex.plugin.TebexPlugin;
import io.tebex.sdk.obj.QueuedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class JoinListener implements Listener {
    private final TebexPlugin plugin;

    public JoinListener(TebexPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerConnect(LoginEvent event) {
        UUID uuid = event.getConnection().getUniqueId();
        String name = event.getConnection().getName();

        Object playerId = plugin.getPlayerId(name, uuid);

        if (!plugin.getQueuedPlayers().containsKey(playerId)) {
            return;
        }

        plugin.handleOnlineCommands(new QueuedPlayer(plugin.getQueuedPlayers().get(playerId), name, uuid.toString()));
    }
}
