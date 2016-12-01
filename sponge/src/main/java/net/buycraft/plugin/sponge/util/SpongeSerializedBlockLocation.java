package net.buycraft.plugin.sponge.util;

import lombok.experimental.UtilityClass;
import net.buycraft.plugin.shared.config.signs.storage.SerializedBlockLocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class SpongeSerializedBlockLocation {
    public static SerializedBlockLocation create(Location<World> location) {
        return new SerializedBlockLocation(location.getExtent().getUniqueId().toString(), location.getBlockX(), location.getBlockY(),
                location.getBlockZ());
    }

    public static Location<World> toSponge(SerializedBlockLocation location) {
        Optional<World> world = Sponge.getServer().getWorld(UUID.fromString(location.getWorld()));
        if (!world.isPresent()) {
            throw new IllegalStateException();
        }
        return new Location<>(world.get(), location.getX(), location.getY(),
                location.getZ());
    }
}
