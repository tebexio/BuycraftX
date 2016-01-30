package net.buycraft.plugin.bukkit.signs.purchases;

import lombok.Value;
import net.buycraft.plugin.bukkit.util.SerializedBlockLocation;

@Value
public class RecentPurchaseSignPosition {
    private final SerializedBlockLocation location;
    private final int position;
}
