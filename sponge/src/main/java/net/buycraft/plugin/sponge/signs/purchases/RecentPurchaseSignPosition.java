package net.buycraft.plugin.sponge.signs.purchases;

import lombok.Value;
import net.buycraft.plugin.bukkit.util.SerializedBlockLocation;

@Value
public class RecentPurchaseSignPosition {
    private final SerializedBlockLocation location;
    private final int position;
}
