package net.buycraft.plugin.sponge.signs.buynow;

import lombok.Value;
import net.buycraft.plugin.sponge.util.SerializedBlockLocation;

@Value
public class SavedBuyNowSign {
    private final SerializedBlockLocation location;
    private final int packageId;
}
