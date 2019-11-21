package net.buycraft.plugin.bukkit;

import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.ServerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;

public class BuycraftListener implements Listener {
    private final BuycraftPluginBase plugin;

    public BuycraftListener(final BuycraftPluginBase plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfiguration().isDisableBuyCommand()) {
            for (String s : plugin.getConfiguration().getBuyCommandName()) {
                if (event.getMessage().substring(1).equalsIgnoreCase(s) ||
                        event.getMessage().regionMatches(true, 1, s + " ", 0, s.length() + 1)) {
                    event.setCancelled(true);
                    plugin.getViewCategoriesGUI().open(event.getPlayer());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
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
