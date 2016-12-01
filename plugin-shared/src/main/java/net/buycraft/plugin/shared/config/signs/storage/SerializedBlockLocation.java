package net.buycraft.plugin.shared.config.signs.storage;

import lombok.Value;

@Value
public class SerializedBlockLocation {
    private final String world;
    private final int x;
    private final int y;
    private final int z;
}
