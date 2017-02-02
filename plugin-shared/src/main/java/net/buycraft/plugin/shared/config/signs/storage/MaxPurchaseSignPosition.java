package net.buycraft.plugin.shared.config.signs.storage;

import lombok.Value;

@Value
public class MaxPurchaseSignPosition {
    private final SerializedBlockLocation location;
    private final int time;
    private final int position;
}
