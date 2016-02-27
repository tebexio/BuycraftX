package net.buycraft.plugin.platform.standalone.runner;

import lombok.experimental.UtilityClass;
import net.buycraft.plugin.data.QueuedPlayer;

@UtilityClass
public class StandaloneUtilities {
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
}
