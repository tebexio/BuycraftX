package net.buycraft.plugin.data;

import lombok.Value;

@Value
public class QueuedPlayer {
    private final int id;
    private final String name;
    private final String uuid;
}
