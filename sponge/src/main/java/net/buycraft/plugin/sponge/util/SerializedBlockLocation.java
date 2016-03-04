package net.buycraft.plugin.sponge.util;

import lombok.Value;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Value
public class SerializedBlockLocation {
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public static SerializedBlockLocation fromSpongeLocation(Location<World> location) {
        return new SerializedBlockLocation(location.getExtent().getName(), location.getBlockX(), location.getBlockY(),
                location.getBlockZ());
    }

    public Location<World> toSpongeLocation() {
        Optional<World> worldOptional = Sponge.getServer().getWorld(world);
        if (!worldOptional.isPresent()) {
            throw new IllegalStateException("This location refers to non-existent world " + world);
        }
        return new Location<>(worldOptional.get(), x, y, z);
    }
}
