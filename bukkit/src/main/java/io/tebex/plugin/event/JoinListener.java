package io.tebex.plugin.event;

import io.tebex.plugin.TebexPlugin;
import io.tebex.sdk.obj.QueuedPlayer;
import io.tebex.sdk.obj.ServerEvent;
import io.tebex.sdk.obj.ServerEventType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Date;

public class JoinListener implements Listener {
    private final TebexPlugin plugin;

    public JoinListener(TebexPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Object playerId = plugin.getPlayerId(player.getName(), player.getUniqueId());
        plugin.getServerEvents().add(new ServerEvent(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), ServerEventType.JOIN, new Date().toString()));

        if(! plugin.getQueuedPlayers().containsKey(playerId)) {
            return;
        }

        plugin.handleOnlineCommands(new QueuedPlayer(plugin.getQueuedPlayers().get(playerId), player.getName(), player.getUniqueId().toString()));
    }
}
