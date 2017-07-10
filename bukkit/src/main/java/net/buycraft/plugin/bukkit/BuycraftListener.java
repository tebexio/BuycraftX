package net.buycraft.plugin.bukkit;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.PlayerCommandExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import java.io.IOException;
import net.buycraft.plugin.client.ApiException;
import java.util.logging.Level;


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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

        if (!plugin.getConfiguration().isDisableVerifyCommand()) {
            String cmd = "bcverify";
            if (event.getMessage().substring(1).equalsIgnoreCase(cmd) ||
                    event.getMessage().regionMatches(true, 1, cmd + " ", 0, cmd.length() + 1)) {
                String code = event.getMessage().substring(cmd.length()+2).trim();
                plugin.getPlatform().log(Level.INFO, "Do verification of " + event.getPlayer().getPlayerListName()  + " against " + code);
                try {
                    plugin.getApiClient().verifyUser(event.getPlayer().getPlayerListName(), code);
                } catch (IOException | ApiException e) {
                    plugin.getPlatform().log(Level.SEVERE, "Unable to mark commands as completed", e);
                    // TODO: Retry?
                }
                event.setCancelled(true);
            }
        }

        if (!plugin.getConfiguration().isDisableBuyCommand()) {
            for (String s : plugin.getConfiguration().getBuyCommandName()) {
                if (event.getMessage().substring(1).equalsIgnoreCase(s) ||
                        event.getMessage().regionMatches(true, 1, s + " ", 0, s.length() + 1)) {
                    plugin.getViewCategoriesGUI().open(event.getPlayer());
                    event.setCancelled(true);
                }
            }
        }
    }
}
