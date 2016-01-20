package net.buycraft.plugin.bukkit;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.tasks.PlayerLoginExecution;
import net.buycraft.plugin.data.QueuedPlayer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
            plugin.getLogger().info(String.format("Executing login commands for %s...", event.getPlayer().getName()));
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new PlayerLoginExecution(qp, plugin));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (StringUtils.equalsIgnoreCase(event.getMessage().substring(1), plugin.getConfiguration().getBuyCommandName()) ||
                StringUtils.startsWithIgnoreCase(event.getMessage().substring(1), plugin.getConfiguration().getBuyCommandName() + " ")) {
            plugin.getViewCategoriesGUI().open(event.getPlayer());
            event.setCancelled(true);
        }
    }
}
