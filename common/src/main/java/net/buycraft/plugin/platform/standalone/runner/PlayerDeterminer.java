package net.buycraft.plugin.platform.standalone.runner;

import net.buycraft.plugin.data.QueuedPlayer;

public interface PlayerDeterminer {
    boolean isPlayerOnline(QueuedPlayer player);
    int getFreeSlots(QueuedPlayer player);
}
