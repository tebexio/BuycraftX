package net.buycraft.plugin.sponge.logging;


import lombok.AllArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;

import java.util.logging.Level;

@AllArgsConstructor
public class LoggerUtils {
    private final BuycraftPlugin plugin;

    public void log(Level level, String message) {
        log(level, message, null);
    }

    public void log(Level level, String message, Throwable e) {
        plugin.getLogger().info(message, e);
    }
}
