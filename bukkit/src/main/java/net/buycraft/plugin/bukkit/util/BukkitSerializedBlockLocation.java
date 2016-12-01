package net.buycraft.plugin.bukkit.util;

import lombok.experimental.UtilityClass;
import net.buycraft.plugin.shared.config.signs.storage.SerializedBlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@UtilityClass
public class BukkitSerializedBlockLocation {
    public static SerializedBlockLocation create(Location location) {
        return new SerializedBlockLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(),
                location.getBlockZ());
    }

    public static Location toBukkit(SerializedBlockLocation location) {
        return new Location(Bukkit.getWorld(location.getWorld()), location.getX(), location.getY(),
                location.getZ());
    }
}
