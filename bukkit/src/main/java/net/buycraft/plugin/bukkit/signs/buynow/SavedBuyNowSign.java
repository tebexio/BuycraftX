package net.buycraft.plugin.bukkit.signs.buynow;

import lombok.Value;
import net.buycraft.plugin.bukkit.util.SerializedBlockLocation;

@Value
public class SavedBuyNowSign {
    private final SerializedBlockLocation location;
    private final int packageId;
}
