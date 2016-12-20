package net.buycraft.plugin.sponge;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.PlayerCommandExecutor;
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
            plugin.getPlayerJoinCheckTask().queue(qp);
        }
    }
}
