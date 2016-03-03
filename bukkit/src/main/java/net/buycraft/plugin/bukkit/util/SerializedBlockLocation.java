package net.buycraft.plugin.bukkit.util;

import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Value
public class SerializedBlockLocation {
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public static SerializedBlockLocation fromBukkitLocation(Location location) {
        return new SerializedBlockLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(),
                location.getBlockZ());
    }

    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
