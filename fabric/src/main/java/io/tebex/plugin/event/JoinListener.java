package io.tebex.plugin.event;

import io.tebex.plugin.TebexPlugin;
import io.tebex.sdk.obj.QueuedPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;

public class JoinListener {
    private final TebexPlugin plugin;

    public JoinListener(TebexPlugin plugin) {
        this.plugin = plugin;
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.player));
    }

    private void onPlayerJoin(PlayerEntity player) {
        Object playerId = plugin.getPlayerId(player.getName().asString(), player.getUuid());
        if(! plugin.getQueuedPlayers().containsKey(playerId)) {
            return;
        }

        plugin.handleOnlineCommands(new QueuedPlayer(plugin.getQueuedPlayers().get(playerId), player.getName().asString(), player.getUuid().toString()));
    }
}

