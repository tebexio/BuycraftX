package net.buycraft.plugin.nukkit.logging;

import cn.nukkit.plugin.PluginLogger;

import java.util.logging.Level;

public class LoggerUtils {
    private final PluginLogger logger;

    public LoggerUtils(final PluginLogger logger) {
        this.logger = logger;
    }

    public void log(Level level, String message) {
        log(level, message, null);
    }

    public void log(Level level, String message, Throwable e) {
        logger.info(message, e);
    }
}
