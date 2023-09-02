package io.tebex.plugin.event;

import io.tebex.plugin.TebexPlugin;
import io.tebex.sdk.obj.QueuedPlayer;
import io.tebex.sdk.obj.ServerEvent;
import io.tebex.sdk.obj.ServerEventType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Date;

public class JoinListener {
    private final TebexPlugin plugin;

    public JoinListener(TebexPlugin plugin) {
        this.plugin = plugin;
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.player));
    }

    private void onPlayerJoin(ServerPlayerEntity player) {
        Object playerId = plugin.getPlayerId(player.getName().asString(), player.getUuid());
        plugin.getServerEvents().add(new ServerEvent(player.getUuid().toString(), player.getName().asString(), player.getIp(), ServerEventType.JOIN, new Date().toString()));

        if(! plugin.getQueuedPlayers().containsKey(playerId)) {
            return;
        }

        plugin.handleOnlineCommands(new QueuedPlayer(plugin.getQueuedPlayers().get(playerId), player.getName().asString(), player.getUuid().toString()));
    }
}

