package net.buycraft.plugin.platform.standalone.runner;

import net.buycraft.plugin.data.QueuedPlayer;

public interface PlayerDeterminer {
    /**
     * Determines whether or not a player is currently online.
     *
     * @param player the player
     * @return whether or not a player is currently online
     */
    boolean isPlayerOnline(QueuedPlayer player);

    /**
     * Determines how many slots this player has available in their inventory.
     *
     * @param player the player
     * @return number of slots available, or {@code -1} if it is not supported or the player is offline
     */
    int getFreeSlots(QueuedPlayer player);
}
