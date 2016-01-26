package net.buycraft.plugin.bukkit.signs;

import lombok.Value;
import net.buycraft.plugin.bukkit.util.SerializedBlockLocation;

@Value
public class PurchaseSignPosition {
    private final SerializedBlockLocation location;
    private final int position;
}
