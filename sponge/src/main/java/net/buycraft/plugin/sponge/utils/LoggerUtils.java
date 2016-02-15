package net.buycraft.plugin.sponge.utils;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;

import java.util.logging.Level;

/**
 * Created by meyerzinn on 2/14/16.
 */
@AllArgsConstructor
public class LoggerUtils {

    private final BuycraftPlugin plugin;

    public void log(Level level, String message) {
        log(level, message, null);
    }

    public void log(Level level, String message, Exception e) {
        if (level == Level.INFO) {
            plugin.getLogger().info(message, e);
        } else if (level == Level.WARNING) {
            plugin.getLogger().warn(message, e);
        } else if (level == Level.SEVERE) {
            plugin.getLogger().error(message, e);
        }
    }
}
