package net.buycraft.plugin.bukkit;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.tasks.PlayerLoginExecution;
import net.buycraft.plugin.data.QueuedPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new PlayerLoginExecution(qp, plugin));
        }
    }
}
