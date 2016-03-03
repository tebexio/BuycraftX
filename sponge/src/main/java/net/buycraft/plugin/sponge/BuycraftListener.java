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
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        if (plugin.getApiClient() == null) {
            return;
        }

        QueuedPlayer player = plugin.getDuePlayerFetcher().fetchAndRemoveDuePlayer(event.getTargetEntity().getName());
        if (player != null) {
            plugin.getLogger().info(String.format("Executing login commands for %s...", player.getName()));
            plugin.getPlatform().executeAsync(new PlayerLoginExecution(player, plugin.getPlatform()));
        }
    }
}
