package net.buycraft.plugin.sponge.signs.purchases;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Value
@AllArgsConstructor
public class RecentPurchaseSignPosition {

    private final Location<World> location;
    private final int position;


}
