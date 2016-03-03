package net.buycraft.plugin.sponge;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.PlayerLoginExecution;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

@RequiredArgsConstructor
public class BuycraftListener {

    private final BuycraftPlugin plugin;

    @Listener
    public void onPlayerJoinEvent(ClientConnectionEvent.Join event) {
        if (plugin.getApiClient() == null) {
            return;
        }
        QueuedPlayer qp = plugin.getDuePlayerFetcher().fetchAndRemoveDuePlayer(event.getTargetEntity().getName());
        if (qp != null) {
            plugin.getLogger().info(String.format("Executing login commands for %s...", event.getTargetEntity().getName()));
            plugin.getPlatform().executeAsync(new PlayerLoginExecution(qp, plugin.getPlatform()));
        }
    }
}
