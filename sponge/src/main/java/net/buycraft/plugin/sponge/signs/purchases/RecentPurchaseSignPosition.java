package net.buycraft.plugin.sponge.signs.purchases;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.buycraft.plugin.sponge.util.SerializedBlockLocation;

@Value
@AllArgsConstructor
public class RecentPurchaseSignPosition {

    private final SerializedBlockLocation location;
    private final int position;


}
