package net.buycraft.plugin.shared.config.signs.storage;

import lombok.Value;

@Value
public class RecentPurchaseSignPosition {
    private final SerializedBlockLocation location;
    private final int position;
}
