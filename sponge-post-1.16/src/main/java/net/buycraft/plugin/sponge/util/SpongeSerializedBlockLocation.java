package net.buycraft.plugin.sponge.util;

import net.buycraft.plugin.shared.config.signs.storage.SerializedBlockLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;
import java.util.UUID;

public final class SpongeSerializedBlockLocation {
    private SpongeSerializedBlockLocation() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static SerializedBlockLocation create(ServerLocation location) {
        return new SerializedBlockLocation(location.world().key().value(), location.blockX(), location.blockY(), location.blockZ());
    }

    public static ServerLocation toSponge(SerializedBlockLocation location) {
        Optional<ServerWorld> world = Sponge.server().worldManager().world(ResourceKey.minecraft(location.getWorld()));

        if (!world.isPresent()) {
            throw new IllegalStateException();
        }

        return ServerLocation.of(world.get(), location.getX(), location.getY(), location.getZ());
    }
}
