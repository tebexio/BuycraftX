package net.buycraft.plugin.nukkit;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.data.QueuedPlayer;

@RequiredArgsConstructor
public class BuycraftListener implements Listener {
    private final BuycraftPlugin plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getApiClient() == null) {
            return;
        }

        QueuedPlayer qp = plugin.getDuePlayerFetcher().fetchAndRemoveDuePlayer(event.getPlayer().getName());
        if (qp != null) {
            plugin.getPlayerJoinCheckTask().queue(qp);
        }
    }
}
