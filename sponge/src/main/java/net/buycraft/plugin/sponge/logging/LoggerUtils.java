package net.buycraft.plugin.sponge.logging;

import net.buycraft.plugin.sponge.BuycraftPlugin;

import java.util.logging.Level;

public class LoggerUtils {
    private final BuycraftPlugin plugin;

    public LoggerUtils(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void log(Level level, String message) {
        log(level, message, null);
    }

    public void log(Level level, String message, Throwable e) {
        plugin.getLogger().info(message, e);
    }
}
