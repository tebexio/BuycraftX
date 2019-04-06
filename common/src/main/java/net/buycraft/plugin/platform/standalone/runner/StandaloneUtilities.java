package net.buycraft.plugin.platform.standalone.runner;

import net.buycraft.plugin.data.QueuedPlayer;

public final class StandaloneUtilities {
    /**
     * A {@link PlayerDeterminer} that always claims that no players are online.
     */
    public static PlayerDeterminer ALWAYS_OFFLINE_PLAYER_DETERMINER = new PlayerDeterminer() {
        @Override
        public boolean isPlayerOnline(QueuedPlayer player) {
            return false;
        }

        @Override
        public int getFreeSlots(QueuedPlayer player) {
            return -1;
        }
    };

    private StandaloneUtilities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
